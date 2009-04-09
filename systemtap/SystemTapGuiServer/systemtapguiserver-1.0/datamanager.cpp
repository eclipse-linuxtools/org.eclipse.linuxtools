	#include "include/datamanager.hpp"
	#include "include/MailBox.hpp"
	#include "include/common.hpp"
	#include "include/confReader.hpp"
	#include "include/dmerror.hpp"
	#include "include/stapdebug.hpp"
	#include "include/logger.hpp"
	#include "include/subscriptionMgr.hpp"

	#include <sys/time.h>
	#include <sys/types.h>
	#include <sys/select.h>
	#include <sys/socket.h>
	#include <sys/wait.h>
	#include <sys/un.h>
	#include <dirent.h>
	#include <errno.h>
	#include <fcntl.h>
	#include <string.h>
	#include <signal.h>
	#include <stdio.h>
        #include <stdlib.h>
        #include <strings.h>
	#include <unistd.h>

	#include <vector>
	#include <iostream>
	#include <sstream>

	using namespace std;
	using namespace datamanager;

	extern int errno;

	const string DataManager::TMP_DIR = "/var/tmp/";
	const string DataManager::DATAMANAGER_SOCKET = TMP_DIR + "stapguidm";
	const string DataManager::STDOUT_SOCK_BASE = TMP_DIR + "stdout-";
	const string DataManager::STDERR_SOCK_BASE = TMP_DIR + "stderr-";
	DataManager* DataManager::spDataMan = NULL;

	DataManager* DataManager::getInstance( )
	{
		if( DataManager::spDataMan == NULL )
		{
			DataManager::spDataMan = new DataManager( );
		}
		return DataManager::spDataMan;
	}
	void DataManager::deleteInstance( )
	{
		if( DataManager::spDataMan == NULL )
			return;
		DataManager *temp = DataManager::spDataMan;
		DataManager::spDataMan = NULL;
		delete temp;
	}

	DataManager::DataManager( ) : mSetup( false ), mNotificationFd( 0 )
	{ }

	DataManager::~DataManager( )
	{
		vector<struct stap_process_t*>::iterator i, end;

		close( mNotificationFd );

		end = mProcessInfo.end( );
		for( i = mProcessInfo.begin( ); i != end; ++i )
		{
			destroyStapProcess( (*i) );
			delete (*i);
		}
	}

	bool DataManager::generateStatus( int scriptID ) const
	{
		// Stub for future use
		return true;
	}

	void DataManager::run( )
	{
		int stdOut, stdErr, numFd, ret = 0;
		fd_set readSet, temp;
		struct dmRequest req;
		Logger log;

		mNotificationFd = setupSocket( );
		if( mNotificationFd < 0 )
			return;

		FD_ZERO( &readSet );
		FD_SET( mNotificationFd, &readSet );

		numFd = mNotificationFd + 1;

		/*
		 * This is the main loop for this thread, it waits for data to collect,
		 * sends data to the MailBox manager, and manages stap processes.
		 */
		while( true )
		{
			memcpy( &temp, &readSet, sizeof( fd_set ) );
			ret = select( numFd, &temp, NULL, NULL, NULL );
			if( ret < 0 )
			{
				ostringstream err;
				err.str() = ("");
				err << "Invalid return from select: '" << strerror( errno ) <<
					"'" << endl;
				log.log( err.str( ) );
				return;
			}

			// We got a message from the SubscriptionMgr
			if( FD_ISSET( mNotificationFd, &temp ) )
			{
				// new notification from SM
				recv( mNotificationFd, &req, sizeof( req ), MSG_WAITALL );
				ret = handleNotification( &req, stdOut, stdErr );

				if( CREATED_PROCESS == ret )
				{
					//A new Process was started.
					FD_SET( stdOut, &readSet );
					FD_SET( stdErr, &readSet );
					numFd = findHighestFd( ) + 1;
				}
				else if( DESTROYED_PROCESS == ret )
				{
					// A process was destroyed
					FD_CLR( stdOut, &readSet );
					FD_CLR( stdErr, &readSet );
					numFd = findHighestFd( ) + 1;
				}
				else if( SHUTDOWN_DATAMANAGER == ret )
				{
					// Shutdown was requested.
					return;
				}
			}
			else
			{	// some process is writing ...

				vector<struct stap_process_t*>::iterator i, end;
				end = mProcessInfo.end( );

				for( i = mProcessInfo.begin( ); ret > 0 && i != end; ++i )
				{
					struct stap_process_t* curr = (*i);
					/*
					 * This section is not an if/else construct because it is
					 * possible that select has found data on both the stdout and
					 * the stderr socket for a process.
					 */
					int bytesCollected = 0;
					if( FD_ISSET( curr->stdOut, &temp ) )
					{
						ret--;
						bytesCollected = collectData( (*i), false );
						if( bytesCollected > 0 )
						{
							if( !sendData( (*i) ) )
							{
								/*
								 * Send failed to send data to any clients, stop
								 * listening for data
								 */
								destroyStapProcess( curr );
								FD_CLR( curr->stdOut, &readSet );
								FD_CLR( curr->stdErr, &readSet );
								delete curr;
								i = mProcessInfo.erase( i );
								numFd = findHighestFd( ) + 1;
								continue;
							}
						}
					}

					bytesCollected = 0;
					if( FD_ISSET( (*i)->stdErr, &temp ) )
					{
						ret--;
						bytesCollected = collectData( (*i), true );
						if( bytesCollected > 0 )
						{
							if( !sendData( (*i) ) )
							{
								/*
								 * Send failed to send data to any clients, stop
								 * listening for data
								 */
								destroyStapProcess( curr );
								FD_CLR( curr->stdOut, &readSet );
								FD_CLR( curr->stdErr, &readSet );
								delete curr;
								i = mProcessInfo.erase( i );
								numFd = findHighestFd( ) + 1;
								continue;
							}
						}
					}

					/*
					 * The waitpid call is used to check the status of this
					 * process.  waitpid is used instead of kill because if a
					 * process has terminated it will be cleaned up before we are
					 * notified of its status.
					 */

					int status, tmp;
					tmp = waitpid( curr->pid, &status, WNOHANG | WUNTRACED);
					if( tmp > 0 &&
						( WIFEXITED( status ) || WIFSIGNALED( status ) ) )
					{
						// This process has been terminated
						MailBox::getInstance( )->cleanBox( curr->scriptid );
						destroyStapProcess( curr );
						FD_CLR( curr->stdOut, &readSet );
						FD_CLR( curr->stdErr, &readSet );
						delete curr;
						i = mProcessInfo.erase( i );
						numFd = findHighestFd( ) + 1;
					}
				}
			}
		}
	}

	int DataManager::collectData( struct stap_process_t* script, bool isError )
	{
		int fd, ret;
		int* size;
		char* buffer;

		script->isMessageError = isError;
		if( isError )
		{
			fd = script->stdErr;
			buffer = script->errBuffer;
			size = &( script->errSize );
		}
		else
		{
			fd = script->stdOut;
			buffer = script->outBuffer;
			size = &( script->outSize );
		}

		// Reads data
		while( *size < BUFFER_SIZE &&
			( ret = read( fd, buffer + (*size), BUFFER_SIZE - (*size) ) ) > 0 )
			*size += ret;

		if( (*size) > BUFFER_SIZE )
		{
			// Error condition
			ostringstream err;
			if( isError )
				err << "stderr ";
			else
				err << "stdout";

			err << "buffer overrun for script " << script->scriptid;

			Logger log;
			log.log( err.str( ) );
			cerr << err.str( ) << endl;
			*size = 0;
			memset( buffer, 0, BUFFER_SIZE );
			return 0;
		}

		/*
		 * Starts at the back of the buffer and walks towards the front until we
		 * find the first newline, this delimits a quantum of data and we will only
		 * send a complete quantum to the client.  If we don't find a newline then
		 * we will send the entire buffer.
		 */
		while( (*size) >= 0 && buffer[ *size ] != '\n' )
			(*size)--;

		if( (*size) < 0 )
			*size = strlen( buffer );
		else
			(*size)++;

		return (*size);
	}

	/**
	 * Forks a process that will exec stap after establishing socket communication
	 * with the main process and redirecting stdout and stderr to these sockets.
	 * The main process will setup the sockets and listen for incoming connections
	 * on them.
	 *
	 * @param scriptID The ID number of the script to start
	 *
	 * @return A pointer to the new process structure.
	 */
	struct stap_process_t* DataManager::createUserProcess( struct dmRequest *req )
	{
		struct stap_process_t* ret = NULL;
		int pid, fd, len;
		ret = new struct stap_process_t;
		if( ret == NULL )
		{
			Logger log;
			ostringstream err;
			err << "Out of memory" << endl;
			log.log( err.str( ) );
			return ret;
		}

		memset( ret, 0, sizeof( struct stap_process_t ));

		ret->scriptid = req->scriptID;

		if( !setupStapIPC( ret ) )
		{
			delete ret;
			return NULL;
		}

		len = sizeof( struct sockaddr_un );

		pid = fork( );
		if( pid < 0 )
		{
			ostringstream err;
			err << "Failed to fork:" << strerror( errno ) << endl;
			Logger log;
			log.log( err.str( ) );
			delete ret;
			return NULL;
		}
		else if( pid == 0 )
		{
                       setpgid(0,0);
			// Child Process
			execStap( req );
			/*
			 * If execStap returns, the exec call failed and we should terminate
			 * this process.
			 */
                        
			exit( 1 );
		}
		else
		{
			// Parent process
			ret->pid = pid;
			ostringstream os;
			struct sockaddr_un local;
			local.sun_family = AF_UNIX;

			// Accept the stdOut connection
			os << STDOUT_SOCK_BASE << ret->scriptid;
			strcpy( local.sun_path, os.str( ).c_str( ) );
			fd = accept( ret->stdOut, (struct sockaddr*)&local, (socklen_t*)&len );
			if( fd < 0 )
				goto PARENT_PROC_ERR;

			close( ret->stdOut );
			ret->stdOut = fd;

			forceNonBlocking(fd);

			os.str( "" );
			// Accept the stdErr connection
			os << STDERR_SOCK_BASE << ret->scriptid;
			strcpy( local.sun_path, os.str( ).c_str( ) );
			fd = accept( ret->stdErr, (struct sockaddr*)&local, (socklen_t*)&len );
			if( fd < 0 )
				goto PARENT_PROC_ERR;

			forceNonBlocking(fd);

			close( ret->stdErr );
			ret->stdErr = fd;
		}

		return ret;

	PARENT_PROC_ERR:
		ostringstream err;
		err << "Error redirecting stdout or stderr: '" << strerror( errno ) << "'"
			<< endl;
		Logger log;
		log.log( err.str( ) );
		close( ret->stdErr );
		close( ret->stdOut );
		kill( ret->pid, SIGINT );
		delete ret;
		return NULL;
	}

	/**
	 * Close sockets for stdout and stderr, kill the process and remove the socket
	 * files.
	 */
	void DataManager::destroyStapProcess( struct stap_process_t* script )
	{
                char command[1024];
		close( script->stdOut );
		close( script->stdErr );
		ostringstream os;
           //     sprintf(command,"pkill -SIGINT -f .*stapio.*%d.*", script->pid); 
            //    system(command);
                sprintf(command,"pkill -g %d",script->pid);
            //    system(command);
             //   sprintf(command,"kill %d",script->pid);
                if(system(command)) {}
		os << DataManager::STDOUT_SOCK_BASE << script->scriptid;
		unlink( os.str( ).c_str( ) );
		os.str( "" );
		os << DataManager::STDERR_SOCK_BASE << script->scriptid;
		unlink( os.str( ).c_str( ) );

	}

	int DataManager::handleNotification( struct dmRequest *req, int& stdOut, int& stdErr )
	{
		int mailBoxID = req->scriptID;

		// If the ID is negative a shutdown has been requested.
		if( mailBoxID < 0 )
			return SHUTDOWN_DATAMANAGER;

		/*
		 * If the ID is not a valid script to run, nothing to do, not sure if this
		 * should be reported.
		 */
		if( !ConfReader::getInstance( )->isValid( mailBoxID ) )
			return 0;

		vector<struct stap_process_t*>::iterator i, end;
		end = mProcessInfo.end( );
		struct stap_process_t* temp = NULL;

		for( i = mProcessInfo.begin( ); i != end; ++i )
		{
			if( (*i)->scriptid == mailBoxID )
			{
				if( MailBox::getInstance( )->isEmpty( mailBoxID ) )
				{
					/*
					 * The process exists but there are not any clients subscribed
					 * to this box any longer, kill the process.
					 */
					temp = (*i);
					mProcessInfo.erase( i );
					destroyStapProcess( temp );
					stdOut = temp->stdOut;
					stdErr = temp->stdErr;
					delete temp;
					return DESTROYED_PROCESS;
				}
				else
				{
					/*
					 * The process exists and there are clients subscribed,
					 * do nothing
					 */
					return 0;
				}
			}
		}

		/*
		 * The first subscription was added to the specified box, create the
		 * process for it
		 */
		temp = createUserProcess( req );
		if( temp == NULL )
			return 0;
		stdOut = temp->stdOut;
		stdErr = temp->stdErr;
		mProcessInfo.push_back( temp );

		return CREATED_PROCESS;
	}

	/**
	 * Sets up the socket used to comminucate with the SubscriptionMgr
	 */
	int DataManager::setupSocket( )
	{
		int s, fd, ret;
		struct sockaddr_un local;
		s = socket( AF_UNIX, SOCK_STREAM, 0 );
		if(  s < 0 )
		{
			ostringstream err;
			err << "Unable to create dm socket: '" << strerror( errno ) << "'" <<
				endl;
			Logger log;
			log.log( err.str( ) );
			return -1;
		}

		local.sun_family = AF_UNIX;
		strcpy( local.sun_path, DATAMANAGER_SOCKET.c_str( ) );

		ret = bind( s, (struct sockaddr*)&local, sizeof( struct sockaddr_un ) );
		if( ret < 0 )
		{
			ostringstream err;
			err << "Unable to bind dm socket: '" << strerror( errno ) << "'" <<
				endl;
			Logger log;
			log.log( err.str( ) );
			cerr << err.str( ) << endl;
			close( s );
			return -1;
		}


		ret = listen( s , 1 );
		if( ret < 0 )
		{
			ostringstream err;
			err << "Unable to listen to dm socket: '" << strerror( errno ) << "'" <<
				endl;
			Logger log;
			log.log( err.str( ) );
			cerr << err.str( ) << endl;
			close( s );
			return -1;
		}

		ret = sizeof( struct sockaddr_un );
		mSetup = true;

		fd = accept( s, (struct sockaddr*)&local, (socklen_t*)&ret );
		if( fd < 0 )
		{
			ostringstream err;
			err << "Error accepting new dm connection: '" << strerror( errno ) <<
				"'" << endl;
			Logger log;
			log.log( err.str( ) );
			cerr << err.str( ) << endl;
			close( s );
			return -1;
		}
                 //mSetup = true; 
//               if( fd == 0)
  //             {
		forceNonBlocking(fd);

		close( s );
		mSetup = false;

		return fd;
    //          }
	}

	/**
	 * Sets up te sockets to recieve data from this stap process
	 */
	bool DataManager::setupStapIPC( struct stap_process_t* script )
	{
		struct sockaddr_un local;
		int fd, len;
		local.sun_family = AF_UNIX;
		len = sizeof( struct sockaddr_un );
		ostringstream os;

		os << STDOUT_SOCK_BASE << script->scriptid;
		strcpy( local.sun_path, os.str( ).c_str( ) );
		fd = socket( AF_UNIX, SOCK_STREAM, 0 );
		if( fd < 1 )
		{
			ostringstream err;
			err << "Error creating stdout socket: '" << strerror( errno ) << "'"
				<< endl;
			Logger log;
			log.log( err.str( ) );
			return false;
		}

		if( 0 > bind( fd, (struct sockaddr*)&local, len ) )
		{
			ostringstream err;
			err << "Error binding stdout socket: '" << strerror( errno ) << "'"
				<< endl;
			Logger log;
			log.log( err.str( ) );
			close( fd );
			return false;
		}

		if( 0 > listen( fd, 1 ) )
		{
			ostringstream err;
			err << "Error listening on stdout socket: '" << strerror( errno ) << "'"
				<< endl;
			Logger log;
			log.log( err.str( ) );
			close( fd );
			return false;
		}

		script->stdOut = fd;

		os.str( "" );
		os << STDERR_SOCK_BASE << script->scriptid;
		local.sun_path[ 0 ] = 0;
		strcpy( local.sun_path, os.str( ).c_str( ) );
		fd = socket( AF_UNIX, SOCK_STREAM, 0 );
		if( fd < 1 )
		{
			ostringstream err;
			err << "Error creating stderr socket: '" << strerror( errno ) << "'"
				<< endl;
			Logger log;
			log.log( err.str( ) );
			return false;
		}

		if( 0 > bind( fd, (struct sockaddr*)&local, len ) )
		{
			ostringstream err;
			err << "Error binding stderr socket: '" << strerror( errno ) << "'" <<
				" " << local.sun_path << endl;
			Logger log;
			log.log( err.str( ) );
			close( fd );
			return false;
		}

		if( 0 > listen( fd, 1 ) )
		{
			ostringstream err;
			err << "Error listening on stderr socket: '" << strerror( errno ) << "'"
				<< endl;
			Logger log;
			log.log( err.str( ) );
			close( fd );
			return false;
		}

		script->stdErr = fd;

		return true;
	}

	/**
	 * Connects to data sockets, redirects stdout and stderr then calls exec
	 */
	void DataManager::execStap( struct dmRequest *req )
	{
		int stdOut, stdErr;
		struct sockaddr_un local;
		ostringstream os;
		Logger log;
		local.sun_family = AF_UNIX;
		int rc = 0;
                char cmd[75];
                
		const scriptElement* args =
			ConfReader::getInstance( )->getScript( req->scriptID );

		if( args == NULL )
		{
			// failed to find script ...
			ostringstream err;
			err << "Failed to find script number : " << req->scriptID << endl;
			Logger log;
			log.log( err.str( ) );
			return;
		}

		// Set stdout as unbuffered to avoid double buffering
		setvbuf( stdout, NULL, _IONBF, 0 );

		os << STDOUT_SOCK_BASE << req->scriptID;
		strcpy( local.sun_path, os.str( ).c_str( ) );
		stdOut = socket( AF_UNIX, SOCK_STREAM, 0 );
		stdErr = socket( AF_UNIX, SOCK_STREAM, 0 );
		if( stdOut < 0 || stdErr < 0 )
		{
			ostringstream err;
			err << "Error creating output sockets: '" << strerror( errno ) << "'"
				<< endl;
			Logger log;
			log.log( err.str( ) );
			return;
		}

		if( 0 > dup2( stdOut, STDOUT_FILENO ) ||
			0 > dup2( stdErr, STDERR_FILENO ) )
		{
			ostringstream err;
			err << "Failed to dup output: " << strerror (errno) << endl;
			Logger log;
			log.log( err.str( ) );
			return;
		}

		if( 0 > connect( stdOut, (struct sockaddr*)&local,
			sizeof( struct sockaddr_un ) ) )
		{
			ostringstream err;
			err << "Error connecting to stdout socket: '" << strerror( errno ) << "'"
				<< endl;
			Logger log;
			log.log( err.str( ) );
			return;
		}

		os.str( "" );
		os << STDERR_SOCK_BASE << req->scriptID;
		strcpy( local.sun_path, os.str( ).c_str( ) );
		if( 0 > connect( stdErr, (struct sockaddr*)&local,
			sizeof( struct sockaddr_un ) ) )
		{
			ostringstream err;
			err << "Error connecting to stderr socket: '" << strerror( errno ) << "'"
				<< endl;
			Logger log;
			log.log( err.str( ) );
			return;
		}
                         sprintf(cmd,"stap %s",(args->scriptName).c_str());
                             // strip data of excess spaces
                             int i;
                             for(i=0;i<20;i++)
                            if(req->data[i] == ' ') {break;}
                             char username[i];
                             for(int n=0;n<i;n++)
                                  username[n] = req->data[n];
                             username[i]='\0';


                   
		switch(args->scriptType) {
			case (SYSTEM_TAP):
				os.str( "" );
				os << "Executing '" <<  "stap process. " << endl;
				os << "Command: " <<  args->scriptName << ", args = " << args->args.c_str( ) << endl;
				log.log( os.str( ) );
				execlp("staprun",  "staprun", (args->scriptName).c_str( ), (char*)NULL );
			break;
                       case (STP):
                             os.str( "" );
                             os << "Executing " << "stap stp process. "<< endl;
                             os << "Command: " << args->scriptName << endl;
                           // sprintf(cmd,"su %s -c \"stap  %s\"",username,(args->scriptName).c_str());
                             sprintf(cmd,"stap %s", (args->scriptName).c_str());
                             // strip data of excess spaces
                           //  int i;
                           //  for(i=0;i<20;i++)
                           // if(req->data[i] == ' ') {break;}
                           //  char username[i];
                           //  for(int n=0;n<i;n++)
                           //       username[n] = req->data[n];
                           //  username[i]='\0';
                             log.log( os.str( ) );
                              os << cmd << endl;
                             //  system(cmd);
                                 
                               execlp("su","su",username,"-c",cmd,(char*)NULL);
                             break;
                             
		      case (STP_GURU):
                             os.str( "" );
                             os << "Executing " << "stap -g process. "<< endl;
                             os << "Command: " << args->scriptName << endl;
                             log.log( os.str( ) );
                           sprintf(cmd,"stap -g %s",(args->scriptName).c_str());
                         //    execlp("stap -g", "stap", (args->scriptName).c_str( ),(char*)NULL);
                           execlp("su","su",username,"-c",cmd,(char*)NULL);
                               break;
       
                      case (BLUEDYE):
			os.str( "" );
			os << "Exec'ing Bluedye Analyzer: Ensuring Module is in place: " << endl;
			log.log(os.str());
			rc = system("modprobe bluedye");
			if (rc) {
				os.str( "" );
				os << "Error: Bluedye Module Not Found.  Please install Bluedye package." << endl;
				log.log(os.str());
				exit(1);
			}
			os.str( "" );
			os << "Exec'ing: " << args->scriptName << ", args = " << args->args.c_str( ) << endl;
			log.log(os.str());
			execl(args->scriptName.c_str( ), " ", args->args.c_str( ), (char*) NULL );
			break;
		case (SHELL):
			string cmd;
			cmd = args->scriptName + " " + args->args;
			os.str( "" );
			os << "pid: " << getpid() << ", DMD Execing Shell Cmd: " << cmd << endl;
			log.log(os.str());
			if(system(cmd.c_str())) {}
			exit(0);
			break;
	}

	ostringstream err;
	err << "Failed to execute script #: " << cmd << ", Error: " << strerror (errno) << endl;
	log.log( err.str( ) );

	exit( 1 );
}

bool DataManager::sendData( struct stap_process_t* script )
{
	char* buffer;
	int* size;
	struct dmResponse resp;

	/*
	 * buffer has the message data, size has how many bytes we
	 * should send
	 */
	if( script->isMessageError )
	{
		buffer = script->errBuffer;
		resp.src = STDERR;
		size = &( script->errSize );
	}
	else
	{
		buffer = script->outBuffer;
		resp.src = STDOUT;
		size = &( script->outSize );
	}

	resp.beginIdStr = DM_PACKET_BEGIN;
	resp.scriptID = script->scriptid;
	resp.data = buffer;
	resp.returnCode = 0;
	resp.size = (*size) + 1;

	if( !MailBox::getInstance( )-> sendData( &resp ))
	{
		/*
		 * the mailbox appears to be empty, probably because the clients
		 * unexpectedly died off.
		 */
		return false;
	}

	char temp[ DataManager::BUFFER_SIZE + 1 ] = { '\0' };
	strcpy( temp, buffer + (*size) );
	memset( buffer, 0, BUFFER_SIZE );
	strcpy( buffer, temp );
	*size = strlen( buffer );

	return true;
}

int DataManager::findHighestFd( )
{
	int ret = mNotificationFd;
	vector<struct stap_process_t*>::iterator i, end;
	end = mProcessInfo.end( );

	for( i = mProcessInfo.begin( ); i != end; ++i )
	{
		if( (*i)->stdErr >= ret )
			ret = (*i)->stdErr;
		if( (*i)->stdOut >= ret )
			ret = (*i)->stdOut;
	}
	return ret;
}

void* startDataManager( void* data )
{
	DataManager* dm = DataManager::getInstance( );

	/*
	 * This thread will ignore signals, start with a set of ignored signals
	 * that ignores all and remove a few key ones.
	 */
	sigset_t ignore;
	sigfillset( &ignore );
	sigdelset( &ignore, SIGFPE );
	sigdelset( &ignore, SIGSEGV );
	sigdelset( &ignore, SIGBUS );
	pthread_sigmask( SIG_SETMASK, &ignore, NULL );
	dm->run( );

	/*
	 * dm->run only returns if we have a fatal error or we recieve the
	 * shutdown signal.
	 */
	DataManager::deleteInstance( );

	// Remove all the socket files
	unlink( DataManager::DATAMANAGER_SOCKET.c_str( ) );
	struct dirent* info;
	DIR* tmpDir = opendir( DataManager::TMP_DIR.c_str( ) );
	if( tmpDir == NULL )
		return NULL;

	while( ( info = readdir( tmpDir ) ) != NULL )
	{
		string path = DataManager::TMP_DIR + info->d_name;
		if( path.find( DataManager::STDOUT_SOCK_BASE ) != string::npos ||
			path.find( DataManager::STDERR_SOCK_BASE ) != string::npos )
		{
			unlink( path.c_str( ) );
		}
	}

	return NULL;
}

#include "include/subscriptionMgr.hpp"
#include "include/datamanager.hpp"
#include "include/stapdebug.hpp"
#include "include/dmerror.hpp"
#include "include/MailBox.hpp"
#include "include/common.hpp"
#include "include/logger.hpp"
#include "include/confReader.hpp"

#include <iostream>
#include <sstream>
#include <linux/types.h>
#include <linux/socket.h>
#include <netinet/in.h>
#include <pthread.h>
#include <signal.h>
#include <errno.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <strings.h>
#include <sys/un.h>
#include <unistd.h>
#include <errno.h>
#include <sys/stat.h>

using namespace std;
using namespace datamanager;

SubscriptionMgr* SubscriptionMgr::smInstance = NULL;
bool SubscriptionMgr::SYSTEM_EXITING = false;
std::map<int, char* > SubscriptionMgr::users;
char SubscriptionMgr::user[20];


extern char *optarg;
extern int optind, opterr, optopt;

extern int errno;

SubscriptionMgr::SubscriptionMgr() : dmThread( 0 ),
	permSocketfd( -1 ), dmSocketfd( -1 )
{
	/* Create data manager thread */
	pthread_create(&dmThread, NULL, startDataManager, NULL);
	/* init communication with datamanager */
        sleep(1);
 	initDMComm();
}

int SubscriptionMgr::initDMComm()
{
	struct sockaddr_un server;
	int rc;
	DataManager *dm;
	dmSocketfd = socket(AF_UNIX, SOCK_STREAM, 0);
	if (dmSocketfd < 0) {
		ostringstream err;
 		err << "Error calling socket on data manager" << endl;
 		Logger log;
 		log.log(err.str());
		dmdCleanup();
 	}
 	server.sun_family = AF_UNIX;
	strcpy(server.sun_path, DataManager::DATAMANAGER_SOCKET.c_str());

	dm = DataManager::getInstance();
	while (!dm->isSetup()) {}

	rc = connect(dmSocketfd, (struct sockaddr *)&server,
		sizeof(struct sockaddr_un));

	forceNonBlocking(dmSocketfd);

	if (rc) {
		ostringstream err;
 		err << "Error connecting to data manager RC: " << rc << " : "
			<< strerror(errno) << endl;
		Logger log;
 		log.log(err.str());
		dmdCleanup();
	}
	return 0;
}

int SubscriptionMgr::notifyDM(struct dmRequest *req)
{
	unsigned int size = 0;
	int ret;

	while( size < sizeof(req->scriptID) &&
		( ret = write(
			dmSocketfd, req + size, sizeof(*req) - size ) ) > 0 )
		size += ret;

	if (ret < 0) {
 		ostringstream err;
 		err << "Error writing to data manager socket" << endl;
 		Logger log;
 		log.log(err.str());
 		dmdCleanup();
 		return ret;
	}
	return size;
}

SubscriptionMgr * SubscriptionMgr::getInstance()
{
	if (!smInstance)
		smInstance = new SubscriptionMgr();

	return smInstance;
}

int SubscriptionMgr::getStatus(int scriptID, int flags)
{
	return -1;
}

/* runScript
 * @brief: Stub for future expansion.  Will allow custom script execution
 */
int SubscriptionMgr::runScript(char path)
{
	return -1;
}

int SubscriptionMgr::handleRequest(struct dmRequest* req, struct dmResponse* resp, int newfd)
{
	int rc = 0;
	MailBox* mbInstance = MailBox::getInstance ();
	resp->size = 0;
	string str;
	char *buf;
        ostringstream os;
	ConfReader *cr = ConfReader::getInstance();

	if (req->beginIdStr != DM_PACKET_BEGIN)
		rc = DM_ERR_PACKET_FORMAT;
	else if (!mbInstance->isClient (req->clientID) && req->reqAction != REQ_CREATE_CONN)
		rc = DM_ERR_COMM_CHANNEL;
	else {
		switch (req->reqAction) {
			case REQ_CREATE_CONN:
				// A new connection was requested
				mbInstance->addClient (newfd);
                                sprintf(user,"%s",req->data);
                                users[newfd] = user;
				rc = DM_NOTICE_CONNECTION_CREATED;
				break;
				case REQ_DESTROY_CONN:
					// Remove all state related to clientID
					mbInstance->delClient (req->clientID);
					rc = mbInstance->unsubscribeAll (req->clientID);
					if (rc != -1) {
						req->scriptID = rc;
						notifyDM (req);
					}
					rc = DM_NOTICE_CONNECTION_CLOSED;
					break;
				case REQ_SUBSCRIBE:
					int id;
					id = cr->addScript(req);
					req->scriptID = id;
					rc = mbInstance->subscribe(req);
					if (rc == 0) {
				        sprintf(req->data,"%s",users[req->clientID]); 
						notifyDM(req);
						rc = DM_NOTICE_SUB_SUCCESS;
					}
					break;
				case REQ_UNSUBSCRIBE:
					rc = mbInstance->unsubscribe (req->scriptID, req->clientID);

					// unsubscribe will be non-zero if script is invalid
					if (rc == 0 && mbInstance->isEmpty(req->scriptID)) {
						notifyDM (req);
						rc = DM_NOTICE_UNSUB_SUCCESS;
					}

					break;
				case REQ_GET_STATUS:
					// currently not supported
					rc = DM_ERR_REQUEST_TYPE;
					break;
				case REQ_SUBSCRIPTION_MODIFY:
					// currently not supported
					rc = DM_ERR_REQUEST_TYPE;
					break;
				case REQ_CONFIG_FILE:
					cr->readConfFile(str);
					buf = strdup(str.c_str());
					resp->data = buf;
					resp->size = str.length();
					break;

				       
				default:
					rc =  DM_ERR_REQUEST_TYPE;
					break;
			} // switch

		} // else


		resp->beginIdStr = DM_PACKET_BEGIN;
		resp->src = SUBSCRIPTION_MGR;
		resp->scriptID = req->scriptID;

		if (req->reqAction == REQ_CREATE_CONN)
			resp->clientID = newfd;
		else
			resp->clientID = req->clientID;

		resp->returnCode = rc;
		// currently unused
		return rc;
	}

	int SubscriptionMgr::startService()
	{
		struct sockaddr_in serv_addr, client_addr;
		int newfd, rc;
		socklen_t client_len = sizeof(client_addr);
		char buf[255];

		permSocketfd = socket(AF_INET, SOCK_STREAM, 0);
		if (permSocketfd < 0) {
			ostringstream err;
			err << "Error on socket: " << strerror (errno) << endl;
			cerr << err.str() << endl;
			Logger log;
			log.log(err.str());
			return -1;
		}

		memset(&serv_addr, 0, sizeof(serv_addr));
		serv_addr.sin_family = AF_INET;
		serv_addr.sin_addr.s_addr = INADDR_ANY;
		serv_addr.sin_port = htons(LISTEN_PORT);

		if (bind(permSocketfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
			ostringstream err;
			err << "Error on binding: " << strerror (errno) << endl;
			cerr << err.str() << endl;
			Logger log;
			log.log(err.str());
			return -1;
		}

		if (listen(permSocketfd, 5) != 0) {
			ostringstream err;
			err << "Error on listen: " << strerror (errno) << endl;
			cerr << err.str() << endl;
			Logger log;
			log.log(err.str());
			return -1;
		}

		cout << "Listening for connections... " << endl;

	/* Loop looking for new connection requests */
	while (true) {
		struct dmRequest* req;
		struct dmResponse resp;

		newfd = accept(permSocketfd, (struct sockaddr *) &client_addr, &client_len);

		if (newfd < 0) {
			ostringstream err;
			err << "Error on accept: " << strerror (errno) << endl;
			cerr << err.str() << endl;
 			Logger log;
 			log.log(err.str());
			return -1;
		}

		memset(&buf, 0, 255);

		if (recv(newfd, buf, sizeof( struct dmRequest ), MSG_WAITALL) < 0) {
			ostringstream err;
			err << "Error reading from socket: " << strerror (errno) << endl;
			cerr << err.str() << endl;
 			Logger log;
 			log.log(err.str());
			return -1;
		}

	/* convert request to host byte order */
		req = (struct dmRequest*) buf;
                     handleRequest(reqConvToH(req), &resp, newfd);
                

		/* Send response to client */
		respConvToN(&resp);
		send(ntohl(resp.clientID), &resp, sizeof(resp), 0);
		if (resp.size) { /* if there is anything to send */
			send(ntohl(resp.clientID), resp.data, ntohl(resp.size), 0);
		}
		if (req->reqAction != REQ_CREATE_CONN)
			close (newfd);
	}

	return rc;
}

void SubscriptionMgr::dmdCleanup()
{
	void *tmp;
	struct dmRequest req;

	ostringstream out;
 	out << "Closing stapgui daemon\n";
 	Logger log;
 	log.log(out.str());
 	memset(&req, 0, sizeof(req));
 	req.scriptID = SHUTDOWN_DATAMANAGER;
	notifyDM(&req);
	pthread_join(dmThread, &tmp);
	MailBox::getInstance()->deleteInstance();
	close(permSocketfd);
	close(dmSocketfd);
	exit(0);
}

void dmdSighandler(int sig)
{
	SubscriptionMgr::getInstance( )->dmdCleanup();
}

void printUsage( const string& name )
{
	cerr << "Usage: " << name << " [-h] [-b] [-p PORTNUMBER ]" << endl;
	cerr << " -h:            help, this message" << endl;
	cerr << " -b:            Run in background (daemonize the process)" << endl;
	cerr << " -p PORTNUMBER: Use specified port number instead of default (" <<
		LISTEN_PORT << ")" << endl;
}

int main(int argc, char **argv)
{
	struct sigaction sigact;
	SubscriptionMgr* sm = NULL;
	bool daemon = false;
	int port = LISTEN_PORT;
	int opt;

	while ((opt = getopt(argc, argv, "hbp:")) != -1) {
		switch (opt) {
			case 'b':
				daemon = true;
				break;
			case 'p':
				port = atoi(optarg);
				break;
			case 'h':
			default:
				printUsage(argv[0]);
				exit( 1 );
		}
	}

	/* Daemonize child process */
	if (daemon) {
		if(fork()) return 0;
		setsid();
		umask(0);
		// Close stdout, stderr, stdin
		close(0);
		close(1);
		close(2);
	}

	try {
		sm = SubscriptionMgr::getInstance();
	}
	catch ( exception& e )
	{
		ostringstream err;
 		err << "Error creating subscription manager" << endl;
 		Logger log;
 		log.log(err.str());
		return -1;
	}

	/* Setup signal interception, so when process is killed, it cleans
	 * up properly */
	sigact.sa_handler = dmdSighandler;
	sigemptyset(&sigact.sa_mask);

	sigaction(SIGABRT, &sigact, NULL);
	sigaction(SIGILL, &sigact, NULL);
	sigaction(SIGINT, &sigact, NULL);
	sigaction(SIGQUIT, &sigact, NULL);
	sigaction(SIGTERM, &sigact, NULL);

	sm->startService();
	sm->dmdCleanup();

	return 0;
}

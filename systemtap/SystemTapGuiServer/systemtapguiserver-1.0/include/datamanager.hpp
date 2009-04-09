/*
 * License stuff goes here.
 */

#ifndef DATAMANAGER_H_
#define DATAMANAGER_H_

#include <string>
#include <vector>

using namespace std;

namespace datamanager
{

	/**
	 * The DataManager class implements the Singleton pattern.  It will be
	 * responsible for collecting data quanta from the open stap process and
	 * passing that data to the MailBox class for distribution.  DataManager
	 * will also handle start up and shutdown of stap processesat the appropriate
	 * time.
	 *
	 * @Class DataManager
	 *
	 * @ingroup stapguidm
	 *
	 * @brief Handles startup, shutdown, and data collection for stap scripts.
	 */

	class DataManager
	{
		public:

			~DataManager( );

			/**
			 * Gets the status of the specified script
			 *
			 * @param scriptID
			 * 	The id number for the script to query.
			 *
			 * @return
			 * 	True if the process is alive or has been killed normally,
			 *  otherwise false.
			 */
			bool generateStatus( int scriptID ) const;

			/**
			 * Main DataManager method.  Runs a tight loop using select
			 * for notification that data is ready to be collected.
			 */
			void run( );

			/**
			 * Returns setup status for the DataManager
			 *
			 * @return
			 * 	true if setup is finished, false otherwise
			 */
			inline bool isSetup( ) { return mSetup; }

			/**
			 * Returns a reference to the DataManager, constructs one if
			 * necessary.
			 *
			 * @return
			 * 	Refence to the DataManager.
			 */
			static DataManager* getInstance( );

			/**
			 * Deletes the instance of DataManager and sets pointer to NULL.
			 */
			static void deleteInstance( );

			static const int BUFFER_SIZE = 4095;
			static const int CREATED_PROCESS = 1;
			static const int DESTROYED_PROCESS = 2;
			static const string DATAMANAGER_SOCKET;
			static const string STDOUT_SOCK_BASE;
			static const string STDERR_SOCK_BASE;
			static const string TMP_DIR;

		private:
			static DataManager* spDataMan;

			bool mSetup;
			int mNotificationFd;
			vector<struct stap_process_t*> mProcessInfo;

			DataManager( );
			int collectData( struct stap_process_t* script, bool isError = false );
			struct stap_process_t* createUserProcess( struct dmRequest *req );
			void destroyStapProcess( struct stap_process_t* script );
			int handleNotification( struct dmRequest *req, int& stdOut, int& stdErr );
			int setupSocket( );
			int findHighestFd( );
			bool setupStapIPC( struct stap_process_t* script );
			void execStap( struct dmRequest *req );
			bool sendData( struct stap_process_t* script );
	};

	struct stap_process_t
	{
		int pid;
		int scriptid;
		int stdOut;
		int stdErr;
		int outSize;
		int errSize;
		bool isMessageError;
		char outBuffer[ DataManager::BUFFER_SIZE + 1 ];
		char errBuffer[ DataManager::BUFFER_SIZE + 1 ];
	};
}

/**
 * This method is for the call to pthread_create, it will not take any
 * actual input nor will it return anything but NULL (the void*s are for
 * compatibility with the pthread_create call).
 */
void* startDataManager( void* data );

#endif /*DATAMANAGER_H_*/

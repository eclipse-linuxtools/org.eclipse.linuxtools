#ifndef SUBSCRIPTIONMGR_H_
#define SUBSCRIPTIONMGR_H_

#include "datamanager.hpp"
#include "MailBox.hpp"
#include <linux/types.h>
#include <pthread.h>
#include <map>

namespace datamanager
{
	/* @class subscriptionMgr
	 * @brief This class is responsible for managing all incoming
	 * requests for connections / communication with the stap-gui
	 * data manager
	 */
	class SubscriptionMgr
	{
		public:
			static bool SYSTEM_EXITING;

			~SubscriptionMgr();
			int startService();

			/* Accession methods for the subscritpionMap, linking a unique
			 * subscriber ID to a socket file descriptor */
			int addSubscriberID(int uniqueID, int socketfd);
			int removeSubscriberID(int uniqueID);
			int getSubscriberSocketFD(int uniqueID);

			/* getInstance
			 * @brief Returns instance of singleton SubscriptionMgr class, or
			 * 	constructs and returns as needed
			 */
			static SubscriptionMgr *getInstance();

			/* getStatus
			 * @brief For future use - allows a request for status to come from
			 * the dashboard, which then also spawns cleanup in the
			 * DM
			 * param scriptID - identifier for the script to query about
			 * param flags - bitmap of flag options
			 * @return 0 if successful, otherwise a negative error code*/
			int getStatus(int scriptID, int flags);
			/* runScript
			 * @brief For future use - for execution of custom scripts
			 * @param path - Path and script name to be compiled and insmod'd
			 * @return 0 if successful, otherwise a negative error code*/
			int runScript(char path);

			/* dmdSighandler
			 * @brief Cleans up whenever the server is being shut down
			 *  by a signal
			 */
			void dmdCleanup();

		private:
			static SubscriptionMgr *smInstance;
                        static std::map<int, char *> users;
                        static char user[20]; 
			pthread_t dmThread;
			int permSocketfd, dmSocketfd;

			/* default constructor available only to members or
				friends of this class*/
			SubscriptionMgr();
			/* disallow copy constructor */
		    SubscriptionMgr(const SubscriptionMgr &old);
		    /* disallow assignment operator */
		    const SubscriptionMgr &operator=(const SubscriptionMgr &old);

			/* initDMComm
			 * @brief Initiates socket communication with data manager
			 * @return - number of bytes written, or negative error code on
			 *  failure
			 */
			int initDMComm();
			/* notiftyDM
			 * @brief Sends the mailbox ID for which the linked list has
			 * been changed to the data manager
			 * @param sigType - Either a scriptID, or if negative, a request
			 * 	for some predefined action (ie - shutdown)
			 * 	list being modified
			 * @return 0 if successful, otherwise a negative error code*/
			int notifyDM(struct dmRequest *req);
			/* subscribe
			 * @brief Handles modification of the array shared with the
			 * data manager, adding a new socket descriptor to the linked list
			 * @param scriptID - identifier for the script being subscribed to
			 * @return 0 if successful, otherwise a negative error code*/
			int subscribe(int scriptID);
			/* unSubscribe
			 * @brief Handles modification of the array shared with the
			 * data manager, removing a socket descriptor from the linked list
			 * @param scriptID - identifier for the script being unsubscribed
			 * 	from
			 * @return 0 if successful, otherwise a negative error code
			 * */
			int unSubscribe(int scriptID);

			int handleRequest(struct dmRequest* req, struct dmResponse* resp, int newfd);

	};
}

#endif /*SUBSCRIPTIONMGR_H_*/

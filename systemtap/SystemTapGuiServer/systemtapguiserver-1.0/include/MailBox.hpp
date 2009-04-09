#ifndef MAILBOX_HPP_
#define MAILBOX_HPP_

#include <map>
#include <set>

#include <pthread.h>
#include "common.hpp"

namespace datamanager
{
	class MailBox
	{
		friend class SubscriptionMgr;

		private:
//		public:
			// only instance of this class
			static MailBox* spMailBox;

			// maps and iterators for the mailbox and mutexes
			static std::map<int, std::set<int> > boxes;
			static std::map<int, pthread_mutex_t> locks;
			static std::set<int> fds;

			static std::map<int, std::set<int> >::iterator m_it;
			static std::map<int, pthread_mutex_t>::iterator pit;

			/**
			 * Add a "subscriber" to a mailbox, which will receive the output
			 * of a running process. A subscriber is an open file descriptor.
			 *
			 * @param  boxnum	The mailbox number, isValidMod(boxnum) must return true.
			 * @param  socketfd	The file descriptor for the open socket that is to be written to.
			 * @return STAPDM_MBOX_[INVALID,DUPLICATE,LOCK] for invalid mailbox, duplicate
			 *         subscription request, and mutex un/locking failures reespectively.
			 *         0 on success.
			 */
			int subscribe (struct dmRequest *req);

			/**
			 * Remove a file descriptor from the given mailbox. The file descriptor will
			 * no longer be written to.
			 *
			 * @param  boxnum	The mailbox number that the file descriptor is in.
			 * @param  socketfd	The file descriptor to be removed.
			 * @return STAPDM_MBOX_[INVALID,DUPLICATE,LOCK] for invalid mailbox, duplicate
			 *         subscription request, and mutex un/locking failures reespectively.
			 *         0 on success.
			 */
			int unsubscribe (int boxnum, int socketfd);

			/**
			 * Remove the given clientID/socket fd from all mailboxes. This most likely
			 * happens because a client has closed communication with the data manager.
			 *
			 * @param  socketfd	The clientID to be removed from all mailboxes.
			 * @return	The last mailbox from which socketfd was removed and size equals 0
			 * 			or, -1 if that condition was never true.
			 */
			int unsubscribeAll(int socketfd);

			/**
			 * Given a clientID from a request or response packet, this tells if it has been
			 * added before to the set of known IDs.
			 *
			 * @param  socketfd	The clientID/socket fd to query
			 * @return true if it has been previously added, false otherwise
			 */
			bool isClient (int socketfd) const;

			/**
			 * Given a clientID/socket fd, add it to the set of previously seen clientIDs.
			 *
			 * @param  socketfd	A clientID/socket fd, most likely from a dmRequest packet
			 * return  true if the client was added successfully, false otherwise
			 */
			bool addClient (int socketfd);

			/**
			 * Given a clientID/socket fd, remove it from the set of previously seen clientIDs.
			 *
			 * @param	socketfd  A clientID/socket fd, most likely from a dmRequest packet
			 * @return	true if the clientID was removed from the set, false otherwise
			 */
			bool delClient (int socketfd);

			/**
			 * Delete the instance of the mailbox: close all open file descriptors
			 * and optionally send a message to each client notifying them of the
			 * imminent closure of the data manager.
			 */
			void deleteInstance ();

		public:

			/**
			 * The destructor does nothing, this might be removed in the future.
			 */
			~MailBox ();

			/**
			 * Returns a pointer to an internally maintained <code>MailBox</code>.
			 */
			static MailBox* getInstance ();

			/**
			 * Determines if the given mailbox has any subscribers.
			 *
			 * @return	true, if there are zero subscribers, false otherwise.
			 */
			bool isEmpty(int boxnum) const;

			/**
			 * Send data of length size to all clients subscribed to boxnum from
			 * stdout (errflag == false) or stderr (errflag == true). If a client
			 * in the mailbox is dead (send error), it is removed.
			 *
			 * @param  boxnum	The mailbox to aggregate the data to.
			 * @param  size		The size in bytes of the data.
			 * @param  data		A pointer to the data to be sent, this is probably an array of chars.
			 * @param  errflag	true if the data came from stderr, false otherwise.
			 * @return true if data was sent and there are still subscribers, false otherwise
			 */
			bool sendData (struct dmResponse *);

			/**
			 * Send a message to all clients subscribed to given boxnum that the
			 * corresponding process is no longer running, and then unsubscribe
			 * all of them, leaving the mailbox empty.
			 *
			 * @param  boxnum	Which mailbox to clear of subscribers.
			 * @return	Zero if everything went fine, non zero otherwise.
			 */
			int cleanBox (int boxnum);

			/**
			 * This will remove and reset all mailboxes and their corresponding locks.
			 */
			void resetMailBoxes ();
	};

}

#endif /*MAILBOX_HPP_*/

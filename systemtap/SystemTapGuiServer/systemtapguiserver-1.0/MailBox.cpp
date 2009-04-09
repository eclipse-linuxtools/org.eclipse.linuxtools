#include "include/stapdebug.hpp"
#include <iostream>
using std::cout;
using std::endl;
using std::cerr;

#include "include/MailBox.hpp"
#include "include/confReader.hpp"
#include "include/dmerror.hpp"
#include "include/common.hpp"
#include "include/logger.hpp"
#include "include/datamanager.hpp"

#include <algorithm>
#include <iostream>
#include <sstream>

#include <unistd.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>

using namespace std;

/*
 * TODO All of the pthread calls that might fail need to do something more than
 * report the error, if they move forward after reporting we have the potential
 * to introduce race conditions.
 */

namespace datamanager
{

	// static initializations
	MailBox* MailBox::spMailBox = NULL;
	std::map<int, std::set<int> > MailBox::boxes;
	std::map<int, pthread_mutex_t> MailBox::locks;
	std::set<int> MailBox::fds;
	std::map<int, std::set<int> >::iterator MailBox::m_it;
	std::map<int, pthread_mutex_t>::iterator MailBox::pit;

	MailBox::~MailBox ()
	{
		std::set<int>::iterator s_it;
		// close all socket file descriptors

		for (m_it = boxes.begin(); m_it != boxes.end(); ++m_it)
			for (s_it = m_it->second.begin(); s_it != m_it->second.end(); ++s_it) {
				// this is where we could send one last packet to clients
				//cerr <<"\tClosing : " <<*s_it <<endl;
				close (*s_it);
			}

//		for (pit = locks.begin(); pit != locks.end(); ++pit)
//			pthread_mutex_unlock (&pit->second) && pthread_mutex_destroy (&pit->second);

	}

	MailBox* MailBox::getInstance (void)
	{
		if (spMailBox == 0) spMailBox = new MailBox ();
		return spMailBox;
	}

	void MailBox::deleteInstance ()
	{
		if (MailBox::spMailBox == NULL)
			return;
		MailBox *temp = MailBox::spMailBox;
		MailBox::spMailBox = NULL;
		delete temp;
	}

	void MailBox::resetMailBoxes ()
	{

		fds.clear();

		for (m_it = boxes.begin(); m_it != boxes.end(); ++m_it)
			m_it->second.clear ();

		boxes.clear();

		for (pit = locks.begin(); pit != locks.end(); ++pit)
			pthread_mutex_destroy (&pit->second);

		locks.clear();
	}

	bool MailBox::isEmpty(int boxnum) const
	{	// *no locking, do not call while unsubscribe is being called
		// *check if the mailbox is valid before calling this method!

		return boxes[boxnum].size() == 0;
	}

	bool MailBox::addClient (int socketfd)
	{
		fds.insert(socketfd);
		return true;
	}

	bool MailBox::delClient (int socketfd)
	{
		return fds.erase(socketfd) != 0;
	}

	bool MailBox::isClient(int socketfd) const
	{
		if (fds.find(socketfd) != fds.end())
			return true;
		else
			return false;
	}

	int MailBox::cleanBox (int boxnum)
	{
		struct dmResponse notice;

		/* This script died or completed - notify consumers and cleanup
		 * mailbox */

		// assemble packet
		notice.beginIdStr = htonl (DM_PACKET_BEGIN);
		notice.src = htonl (DATA_MGR);
		notice.scriptID = htonl (boxnum);
		notice.size = htonl(0);
		notice.returnCode = htonl (DM_NOTICE_SCRIPT_COMPLETED);

		if (pthread_mutex_lock (&locks[boxnum])) {
			ostringstream err;
			err <<"Failed to lock!\n";
			Logger log;
			log.log (err.str ());
		}

		// for each subscriber in mailbox, write data to socketfd
		std::set<int>::iterator end = boxes[boxnum].end();
		std::set<int>::iterator sit = boxes[boxnum].begin();

		for (; sit != end; ++sit) {
			notice.clientID = htonl (*sit);

			send (*sit, &notice, sizeof (notice), 0);
		}

		coutd << "Emptied mbox: " << boxnum <<endl;
		boxes[boxnum].clear();

		if (pthread_mutex_unlock (&locks[boxnum])) {
			ostringstream err;
			err <<"Failed to unlock!\n";
			Logger log;
			log.log (err.str ());
		}

		return 0;
	}

	bool MailBox::sendData (struct dmResponse *resp)
	//(int boxnum, unsigned int size, void* data, bool errflag)
	{
		char buf[DataManager::BUFFER_SIZE];
		bool rc = true;
		struct dmResponse_header header, *ptr;

		if (!ConfReader::getInstance()->isValid(resp->scriptID))
			return false;

		// check if mailbox has subscribers or return false,
		// this should not happen now anymore
		if (boxes[resp->scriptID].empty()) {
			ostringstream err;
			err << "sending data to an empty mailbox!?\n";
			Logger log;
			log.log (err.str ());
			return false;
		}

		header.beginIdStr = htonl(resp->beginIdStr);
		header.returnCode = htonl(resp->returnCode);
		header.scriptID = htonl(resp->scriptID);
		header.size = htonl(resp->size);
		header.src = htonl(resp->src);
		
                ptr = (dmResponse_header *) buf;
		// build concatenated buffer : header + data
		memset(buf, '\0', DataManager::BUFFER_SIZE);
		memcpy(buf, &header, sizeof(header));
		memcpy(buf + sizeof(header), resp->data, resp->size);

		// aquire mutex lock or block
		if (pthread_mutex_lock (&locks[resp->scriptID])) {
			ostringstream err;
			err << "Failed to lock!\n";
			Logger log;
			log.log (err.str ());
		}

		// assign iterators
		std::set<int>::iterator end = boxes[resp->scriptID].end();
		std::set<int>::iterator sit = boxes[resp->scriptID].begin();

		// for each subscriber in mailbox, write data to socketfd
		for (; sit != end; ++sit) {
			/* Add client id info to buffer, now that we have it */
			ptr->clientID = htonl(*sit);

			// if error, assume connection is dead and remove
			if (send (*sit, buf, sizeof (header) + resp->size, 0) < 0) {
				boxes[resp->scriptID].erase (*sit);
				fds.erase (*sit);
			}
		}

		if (boxes[resp->scriptID].size() == 0)
			rc = false;

		// release lock
		if (pthread_mutex_unlock (&locks[resp->scriptID])) {
			ostringstream err;
			err <<"Failed to unlock!\n";
			Logger log;
			log.log (err.str ());
		}

		return rc;

	}

	int MailBox::unsubscribeAll(int socketfd)
	{	// ** not thread safe?
		// ** also, only returns one of possibly many scriptIDs to be stopped by DM

		int rc = -1;

		// remove socketfd from each mailbox
		for (m_it = boxes.begin(); m_it != boxes.end(); ++m_it) {

			if (pthread_mutex_lock (&locks[m_it->first])) {
				ostringstream err;
				err <<"Failed to lock!\n";
				Logger log;
				log.log (err.str ());
			}
			if (m_it->second.erase (socketfd) != 0 && m_it->second.size () == 0)
				rc = m_it->first;
			if (pthread_mutex_unlock (&locks[m_it->first])) {
				ostringstream err;
				err <<"Failed to unlock!\n";
				Logger log;
				log.log (err.str ());
			}
		}

		return rc;
	}

	int MailBox::subscribe(struct dmRequest *req)
	{
		int retval;
		Logger log;
		int boxnum = req->scriptID;
		int socketfd = req->clientID;

		// check if mailbox number is valid or return error
		if (!ConfReader::getInstance()->isValid(boxnum)) {
			return ConfReader::getInstance()->checkScript(boxnum);
		}

		if (boxes.find(boxnum) == boxes.end()) {
			// mailbox is not in here, create
			pthread_mutex_init (&locks[boxnum], NULL);
			coutd << "Created lock on box :" << boxnum <<endl;
		}

		if (pthread_mutex_lock (&locks[boxnum])) {
			ostringstream err;
			err <<"Failed to lock!\n";
			log.log (err.str ());
		}

		retval = boxes[boxnum].insert(socketfd).second;
		coutd << "Added mbox + rx :" << boxnum << " + " << socketfd <<endl;

		if (pthread_mutex_unlock (&locks[boxnum])) {
			ostringstream err;
			err <<"Failed to unlock!\n";
			log.log (err.str ());
		}

		// note if 0 is ever not returned here there are problems
		return retval ? 0 : DM_ERR_MBOX_DUPLICATE;
	}

	int MailBox::unsubscribe(int boxnum, int socketfd)
	{
		int retval;

		// check if mailbox number is valid or return false
		if (!ConfReader::getInstance()->isValid(boxnum))
			return ConfReader::getInstance()->checkScript(boxnum);

		if (pthread_mutex_lock (&locks[boxnum])) {
			ostringstream err;
			err <<"Failed to lock!\n";
			Logger log;
			log.log (err.str ());
		}

		retval = boxes[boxnum].erase(socketfd);
		coutd << "Deleted mbox + rx :" << boxnum << " + " << socketfd <<endl;

		if (pthread_mutex_unlock (&locks[boxnum])) {
			ostringstream err;
			err <<"Failed to unlock!\n";
			Logger log;
			log.log (err.str ());
		}

		return (retval == 1) ? 0 : DM_ERR_MBOX_DUPLICATE;
	}

} // end of datamanager namespace

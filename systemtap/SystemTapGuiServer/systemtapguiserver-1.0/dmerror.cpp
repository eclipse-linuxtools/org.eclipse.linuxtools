
#include "include/dmerror.hpp"
#include <errno.h>
#include <string.h>

const char * dm_error_print(uint32_t err)
{
	if (err == 0)
		return "Success";

	if (!(err & DM_ERROR))
		return strerror((int) err);

	switch (err) {
		case DM_ERR_MBOX_LOCK:
			return "Mailbox can not be locked or unlocked";
		case DM_ERR_MBOX_DUPLICATE:
			return "Notice: Subscription already removed.";
		case DM_ERR_SOCKET_READ:
			return "Error reading from socket";
		case DM_ERR_LISTEN_SOCKET:
			return "Error on listen socket";
		case DM_ERR_PACKET_FORMAT:
			return "Format of packet sent is unreadable.";
		case DM_ERR_BAD_CLIENTID:
			return "Client specifying a non-allocated clientID";
		case DM_ERR_REQUEST_TYPE:
			return "Unknown request type.";
		case DM_ERR_COMM_CHANNEL:
			return "Communications socket not opened.";
		case DM_ERR_SCRIPT_LOAD:
			return "Error: Script failed to compile/insmod.";
		case DM_ERR_SCRIPT_NOEXIST:
			return "Error: Script does not exist.  Possible version difference between client and daemon. ";
		case DM_ERR_SCRIPT_NOINST:
			return "Error: Script has not been installed.  Run 'make install' on server.";
		case DM_NOTICE_SCRIPT_COMPLETED:
			return "Notice: Script/command on remote system has completed and exited.";
		case DM_NOTICE_CONNECTION_CLOSED:
			return "Notice: Connection to Data Management Daemon (DMD) has been closed.";
		case DM_NOTICE_CONNECTION_CREATED:
			return "Notice: Connection to Data Management Daemon (DMD) created.";
		case DM_NOTICE_SUB_SUCCESS:
			return "Notice: Client subscribed to script/command output.";
		case DM_NOTICE_UNSUB_SUCCESS:
			return "Notice: Client unsubscribed to script/command.";
		default:
			return "Unknown error";
	}
}

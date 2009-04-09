#include "include/common.hpp"
#include "include/dmerror.hpp"
#include <netinet/in.h>
#include <stdlib.h>
#include <iostream>
#include <fcntl.h>

using namespace std;
namespace datamanager
{
	/* Convert packet to network byte order */
	struct dmRequest * reqConvToN(struct dmRequest *req)
	{
		/* Only allow conversion once */
		if (req->beginIdStr != DM_PACKET_BEGIN)
			return req;
               cout << "conversion" << req->data;
		req->beginIdStr = htonl(req->beginIdStr);
		req->reqAction = htonl(req->reqAction);
		req->scriptID = htonl(req->scriptID);
		req->clientID = htonl(req->clientID);
		req->size = htonl(req->size);
//                req->data = htonl(req->data);
		return req;
	}

	/* Convert packet to host byte order only if needed */
	struct dmRequest * reqConvToH(struct dmRequest *req)
	{
		if (req->beginIdStr == DM_PACKET_BEGIN)
			return req;
		else if (ntohl(req->beginIdStr) == DM_PACKET_BEGIN) {
			req->beginIdStr = ntohl(req->beginIdStr);
			req->reqAction = ntohl(req->reqAction);
			req->scriptID = ntohl(req->scriptID);
			req->clientID = ntohl(req->clientID);
			req->size = ntohl(req->size);
  //                      req->data = ntohl(req->data);
		}
		return req;
	}

	struct dmResponse * respConvToN(struct dmResponse *resp)
	{
		/* Only allow conversion once */
		if (resp->beginIdStr != DM_PACKET_BEGIN)
			return resp;

		resp->beginIdStr = htonl(resp->beginIdStr);
		resp->clientID = htonl(resp->clientID);
		resp->scriptID = htonl(resp->scriptID);
		resp->size = htonl(resp->size);
		resp->src = htonl(resp->src);
		resp->returnCode = htonl(resp->returnCode);

		return resp;
	}

	struct dmResponse * respConvToH(struct dmResponse *resp)
	{
		if (resp->beginIdStr == DM_PACKET_BEGIN)
			return resp;
		else if (ntohl(resp->beginIdStr) == DM_PACKET_BEGIN) {
			resp->beginIdStr = ntohl(resp->beginIdStr);
			resp->clientID = ntohl(resp->clientID);
			resp->scriptID = ntohl(resp->scriptID);
			resp->size = ntohl(resp->size);
			resp->src = ntohl(resp->src);
			resp->returnCode = ntohl(resp->returnCode);
		}
		return resp;
	}

	void printReq(struct dmRequest *req)
	{
		cout << "Request Packet: " << endl;
		cout << "\treqAction:" << req->reqAction << " - ";
		switch (req->reqAction) {
			case REQ_CREATE_CONN:
				cout << "Create connection." << endl;
				break;
			case REQ_DESTROY_CONN:
				cout << "Destroy connection." << endl;
				break;
			case REQ_SUBSCRIBE:
				cout << " Subscribing." << endl;
				break;
			case REQ_UNSUBSCRIBE:
				cout << " Unubscribing." << endl;
				break;
			case REQ_GET_STATUS:
				cout << " REQ_GET_STATUS." << endl;
				break;
			case REQ_SUBSCRIPTION_MODIFY:
				cout << " REQ_SUBSCRIPTION_MODIFY." << endl;
				break;
			default:
				cout << " Unknown command. " << endl;
		}

		cout << "\tClientID: " << req->clientID << endl;
		cout << "\tScriptID: " << req->scriptID << endl;
		cout << "\tData size: " << req->size << endl;
		cout << "\tData : " << req->data << " -----------------------------" << endl;
	}

	bool printResp(struct dmResponse *resp)
	{
		bool rc = false;
		char *str;

		if (resp == NULL)
			return false;

		cout << "Response Packet: " << endl;

		cout << "\tbeginIdStr: ";
		if (resp->beginIdStr == DM_PACKET_BEGIN) {
			cout << " Verified. " << endl;
			rc = true;
		}
		else {
			char *buf;
			cout << " Failed verification. " << endl;
			cout << "Resp ID: " << resp->beginIdStr << endl;
			cout << "Required ID: " << DM_PACKET_BEGIN << endl;
			buf = (char *) resp;
			cout << "Buffer: \n" << endl;
			for (unsigned int i = 0; i < sizeof(*resp); i++)
				cout << i << " : " << buf[i] << endl;

			return false;
		}

		cout << "\tsrc: " << resp->src;
		switch (resp->src) {
			case STDOUT:
				cout << " STDOUT." << endl;
				break;
			case STDERR:
				cout << " STDERR." << endl;
				break;
			case SUBSCRIPTION_MGR:
				cout << " SUBSCRIPTION_MGR." << endl;
				break;
			case DATA_MGR:
				cout << " DATA_MGR." << endl;
				break;
			case MAILBOX:
				cout << " MAILBOX." << endl;
				break;
			default:
				cout << ": Unknown source. " << endl;
		}
		cout << "\tscriptID: " << resp->scriptID << endl;
		cout << "\tclientID: " << resp->clientID << endl;
		cout << "\tdata size: " << resp->size << endl;
		cout << "\tReturn Code: " << resp->returnCode << " : " << dm_error_print(resp->returnCode) << endl;

		if (resp->size > 0) {
			cout << " \t Data: " << endl;
			str = (char *) resp->data;
			for (unsigned int i = 0; i < resp->size; i++)
				cout << str[i];
			cout << endl;
		}

		cout << " -----------------------------" << endl;

		return rc;
	}

	int forceNonBlocking(int fd)
	{
		int flags;

		/* Force socket to be non-blocking */
		flags = fcntl(fd, F_GETFL, 0);
		if (flags < 0) {
			return -1;
		}
	    fcntl(fd, F_SETFL, flags | O_NONBLOCK);
	    return 0;
	}
}

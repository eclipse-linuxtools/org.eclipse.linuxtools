#ifndef STAPERROR_H_
#define STAPERROR_H_

#include <stdint.h>

/**
 * @page error_codes Error Code Layout
 *
 * Error code format: 0x8cs0nnnn, where:
 *   - c = category
 *   - s = severity
 *   - nnnn = error code; this 16-bit number is sufficient to uniquely identify
 *           the error within the stap data manager
 */

	#define DM_ERROR 0x80000000

	#define DM_ERR_CAT_MASK 0x0F000000
	#define DM_ERR_SEV_MASK 0x00F00000
	#define DM_ERR_CODE_MASK 0x0000FFFF

	#define DM_ERR_CAT_INTERNAL 0x01000000
	#define DM_ERR_CAT_USER 0x02000000
	#define DM_ERR_CAT_SETUP 0x04000000

	#define DM_ERR_SEV_ERR 0x00100000
	#define DM_ERR_SEV_WARN 0x00200000
	#define DM_ERR_SEV_INFO 0x00400000

	/* Error definition */
	#define DM_ERR_REQUEST_TYPE 	(DM_ERROR | DM_ERR_CAT_USER \
			| DM_ERR_SEV_ERR | 0x00000001)
	#define DM_ERR_LISTEN_SOCKET 	(DM_ERROR | DM_ERR_CAT_USER \
			| DM_ERR_SEV_ERR | 0x00000002)
	#define DM_ERR_SOCKET_READ 	(DM_ERROR | DM_ERR_CAT_INTERNAL \
			| DM_ERR_SEV_ERR | 0x00000004)

	#define DM_ERR_MBOX_DUPLICATE 	(DM_ERROR | DM_ERR_CAT_USER \
	 		| DM_ERR_SEV_WARN | 0x00000008)
	#define DM_ERR_MBOX_LOCK 	(DM_ERROR | DM_ERR_CAT_INTERNAL \
			| DM_ERR_SEV_ERR | 0x0000000F)
	#define DM_ERR_PACKET_FORMAT 	(DM_ERROR | DM_ERR_CAT_USER \
			| DM_ERR_SEV_WARN | 0x00000010)
	#define DM_ERR_BAD_CLIENTID 	(DM_ERROR | DM_ERR_CAT_USER \
			| DM_ERR_SEV_WARN | 0x00000011)
	#define DM_ERR_COMM_CHANNEL 	(DM_ERROR | DM_ERR_CAT_USER \
			| DM_ERR_SEV_WARN | 0x00000012)

	#define DM_ERR_SCRIPT_LOAD 	(DM_ERROR | DM_ERR_CAT_INTERNAL \
			| DM_ERR_SEV_ERR | 0x00000014)
	#define DM_ERR_SCRIPT_NOEXIST 	(DM_ERROR | DM_ERR_CAT_USER \
			| DM_ERR_SEV_ERR | 0x00000018)
	#define DM_ERR_SCRIPT_NOINST 	(DM_ERROR | DM_ERR_CAT_USER \
			| DM_ERR_SEV_ERR | 0x0000001F)
	#define DM_NOTICE_SCRIPT_COMPLETED 	(DM_ERROR | DM_ERR_CAT_USER \
			| DM_ERR_SEV_INFO | 0x00000020)
	#define DM_NOTICE_CONNECTION_CLOSED	(DM_ERROR | DM_ERR_CAT_USER \
			| DM_ERR_SEV_INFO | 0x00000021)
	#define DM_NOTICE_CONNECTION_CREATED	(DM_ERROR | DM_ERR_CAT_USER \
			| DM_ERR_SEV_INFO | 0x00000022)
	#define DM_NOTICE_SUB_SUCCESS	(DM_ERROR | DM_ERR_CAT_USER \
			| DM_ERR_SEV_INFO | 0x00000024)
	#define DM_NOTICE_UNSUB_SUCCESS	(DM_ERROR | DM_ERR_CAT_USER \
			| DM_ERR_SEV_INFO | 0x00000028)

	/* Function for looking up error code meaning */
	const char* dm_error_print(uint32_t err);

#endif /*STAPERROR_H_*/

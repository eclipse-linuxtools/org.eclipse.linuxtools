#ifndef LOGGER_H
#define LOGGER_H

#include <string>

#include <syslog.h>
#define L_DEFAULT_HANDLER syslog // replaces fprintf(3) by syslog(3)
#define L_DEFAULT_PARAMS LOG_USER // default priority for syslog info

using namespace std;

namespace datamanager
{

	class Logger
	{
		public:
			/*
			 * Log level will use the constants defined in syslog.h see
			 * syslog(3) for their meanings.
			 */

			Logger( const string& ident = "stapguidm" );

			~Logger();

			inline const string& getIdent( ) const { return mIdent; }

			/**
			 * Sends the specified message to syslogd with the specified
			 * severity level.  The severity level defaults to LOG_NOTICE if
			 * nothing is specified.
			 */
			void log( const string& message, int level = LOG_NOTICE ) const;

		private:
			string mIdent;
	};

}

#endif //LOGGER_H


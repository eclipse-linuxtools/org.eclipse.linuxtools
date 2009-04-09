#include "include/logger.hpp"

namespace datamanager
{
	Logger::Logger( const string& ident ) :
		mIdent( ident )
	{
		openlog( mIdent.c_str( ), LOG_PID, LOG_USER );
	}

	Logger::~Logger( )
	{
		closelog( );
	}

	void Logger::log( const string& message, int level ) const
	{
		syslog( level, "%s", message.c_str( ) );
	}

}


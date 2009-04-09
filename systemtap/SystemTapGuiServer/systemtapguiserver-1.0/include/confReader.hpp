#ifndef CONFREADER_HPP_
#define CONFREADER_HPP_

#include <string>
#include <map>

#include "common.hpp"

namespace datamanager
{
	class ConfReader
	{
		private:
			static ConfReader* crInstance;

			// map of script# -> scriptElement, plus iterators
			std::map<int, scriptElement> scriptList;

			// currently unused
			int scriptMax, scriptCount;

			ConfReader();
			int loadScripts ();
			std::string parseString (const std::string& line, int str_pos);

		public:
			static ConfReader* getInstance ();
			int getScriptCount () const;
			bool isValid (int scriptNum);
			void setInvalid(int scriptNum);
			int checkScript(int scriptNum);
                        int addScript(struct dmRequest* req);
			void resetConf ();
			static int readConfFile(string fileContents);
			const scriptElement* getScript( int index );
	};

}
#endif /*ConfReader_HPP_*/

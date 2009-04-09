#include "include/confReader.hpp"
#include "include/logger.hpp"
#include "include/dmerror.hpp"
#include "include/common.hpp"

#include <iostream>
#include <fstream>
#include <sstream>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

using namespace std;

namespace datamanager
{
	// static intitializations
	ConfReader* ConfReader::crInstance = NULL;

	ConfReader::ConfReader (void) : scriptMax( 0 ), scriptCount( 0 )
	{
		loadScripts();
	}

	void ConfReader::resetConf ()
	{
		// erase everything
		// then reread config file

		scriptList.clear();
		loadScripts();
	}

	ConfReader* ConfReader::getInstance ()
	{
		if (crInstance == NULL) crInstance = new ConfReader();
		return crInstance;
	}

	void ConfReader::setInvalid(int scriptNum)
	{
		if (checkScript(scriptNum) == 1)
			scriptList[scriptNum].valid = DM_ERR_SCRIPT_LOAD;
	}

	/** checkScript
	 * @brief Looks to see if the physical file represented by a script
	 *  index exists, and whether it corresponds to a script that should
	 *  be installed as defined through the script_list.conf file.
	 * @ret Returns 1 if script has been found and appears valid,
	 * 	 otherwise will return error codes defined in dmerror.hpp
	 */
	int ConfReader::checkScript(int scriptNum)
	{
		if ((scriptNum > scriptMax) || (scriptNum < 0)) {
			return DM_ERR_SCRIPT_NOEXIST;
		}
		else if (scriptList[scriptNum].valid == 1) {
			/* already confirmed valid */
			return 1;
		}
		else if (scriptList[scriptNum].valid != -1) {
			/* already confirmed invalid */
			return scriptList[scriptNum].valid;
		}

		/* If script has not yet been checked... */
		for (int n = 0; n <= scriptMax; n++) {
			if (scriptNum == scriptList[n].scriptNum) {
				if ((fopen (scriptList[scriptNum].scriptName.c_str(), "r")) == NULL) {
					scriptList[scriptNum].valid = DM_ERR_SCRIPT_NOINST;
				}
				else {
					scriptList[scriptNum].valid = 1;
				}

				return scriptList[scriptNum].valid;
			}
		}

		scriptList[scriptNum].valid = DM_ERR_SCRIPT_NOEXIST;
		return scriptList[scriptNum].valid;
	}

	bool ConfReader::isValid (int scriptNum)
	{
		return checkScript(scriptNum) == 1;
	}

	const scriptElement* ConfReader::getScript( int scriptnum )
	{
		if (scriptList.find (scriptnum) == scriptList.end())
			return NULL;
		return &scriptList[scriptnum];
	}

	/* parseString
	 * @brief Parses out a specific sub-string from a given line of text.
	 * The line of text must consist of "-delimited sub-strings.  Given
	 * this, and a number reference 'str_pos',  this function returns the sub-
	 * string at position 'str_pos'
	 * Note:  Count starts at 0
	 *
	 * @param str_pos: Position of substring in line
	 * @param out: Reference to a string, which will hold value of substring
	 * at completion
	 */
	std::string ConfReader::parseString(const std::string& line, int str_pos)
	{
		int count = 0;
		size_t pos = 0;
		int beg, len;

		do {
			beg = line.find('"', pos) + 1;
			pos = line.find('"', beg) + 1;
			len = pos - beg;
			count++;
			} while  ((pos != string::npos) && (count < str_pos));

		return line.substr(beg, len - 1);

	}

	int ConfReader::readConfFile(string fileContents)
	{
		ifstream fin (SCRIPTLIST.c_str(), ifstream::in);
		ostringstream str;
		char tmp[512];

		if (fin.fail()) {
			ostringstream err;
			err << "Error loading " << SCRIPTLIST << " file" << endl;
			Logger log;
			log.log( err.str( ) );
			return -1;
		}

		while(fin.good()) {
			fin.get(tmp, 512);
			str << tmp;
		}
		fileContents = str.str();
		return 0;
	}

	int ConfReader::loadScripts()
	{
		using std::ifstream;
		using std::string;
		using std::cout;
		using std::endl;

		ifstream fin (SCRIPTLIST.c_str(), ifstream::in);
		string line;
		char tmp_buf[512];

		if (fin.fail()) {
			ostringstream err;
			err << "Error loading " << SCRIPTLIST << " file" << endl;
			Logger log;
			log.log( err.str( ) );
			return -1;
		}

		while(!fin.eof()) {
			scriptElement cur;

			fin.getline(tmp_buf,512);
			line = string(tmp_buf);

			if ((line.find('#', 0) == string::npos)
				&& (line.length() > 0)) {
				cur.scriptNum = atoi(parseString(line, 1).c_str());
				cur.scriptType = atoi(parseString(line, 2).c_str());
				if (cur.scriptNum > scriptMax) {
					scriptMax = cur.scriptNum;
				}

				cur.scriptName = parseString(line, 3);
				cur.format_str = parseString(line, 4);
				cur.args = parseString(line, 5);
				cur.valid = -1;

				scriptList.insert( pair<int, scriptElement>( cur.scriptNum, cur ) );
			}
		}

		scriptCount = scriptList.size();
		fin.close();
		return 0;
	}

      int ConfReader::addScript(struct dmRequest* req)
     {
           scriptElement cur;
           ostringstream os;
           cur.scriptNum = req->scriptID;
           char inputfilename[40];
           sprintf(inputfilename,"/tmp/%s",req->data);
           cur.scriptName = inputfilename;
           if(req->guru == 1)
         	  cur.scriptType = 5;
           else
          	 cur.scriptType = 4;
           cur.format_str = "";
           cur.args = "";
           scriptMax = scriptMax + 1;
           if(scriptMax < cur.scriptNum)
           	 scriptMax = cur.scriptNum;
           cur.valid = -1;
           scriptList.insert( pair<int,scriptElement>(cur.scriptNum, cur) ); 
           scriptCount = scriptList.size();
           return cur.scriptNum;
     }

} // end of datamanager namespace

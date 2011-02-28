FILE=/etc/group
USER=`whoami`
num=1

#CREATE A BACKUP FILE WITHOUT OVERWRITING ANYTHING
while [ -e "${FILE}.bak${num}" ]; do
	num=$[ ${num} + 1 ]
done

USEREXISTS=`grep stapdev ${FILE}| grep ${USER}`

if [ -z "${USEREXISTS}" ]; then

echo -e "Creating backup of ${FILE} \nat : ${FILE}.bak${num}"


#BACKUP THE FILE AND THEN WRITE THE CURRENT USER TO THE STAPDEV GROUP
echo "Root password required to add your username to the stapdev group."
su -c "cp ${FILE} ${FILE}.bak${num}; gpasswd -a `whoami` stapdev"

fi

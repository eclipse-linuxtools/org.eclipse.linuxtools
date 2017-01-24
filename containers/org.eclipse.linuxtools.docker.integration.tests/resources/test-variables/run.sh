# file run.sh
#!/bin/sh

if [ -z "$FOO" ]; then
    echo "FOO is empty"
else
    echo "FOO is $FOO"
fi

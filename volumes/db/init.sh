#!/bin/bash

mysql -u GCS_user -ppassword <<< "exit"
mysql -u SEARCH_user -ppassword <<< "exit"
mysql -u PS_user -ppassword <<< "exit"
mysql -u AM_user -ppassword <<< "exit"

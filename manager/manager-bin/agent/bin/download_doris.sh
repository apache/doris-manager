#!/bin/bash
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

download_func() {

    local DOWNLOAD_URL=$1
    local DESC_DIR=$2
    local FILENAME="palo"
    if [ -z "$DOWNLOAD_URL" ]; then
        echo "Error: No download url specified for $FILENAME"
        exit 1
    fi
    if [ -z "$DESC_DIR" ]; then
        echo "Error: No dest dir specified for $FILENAME"
        exit 1
    fi
    if [ ! -d $DESC_DIR ]; then
      mkdir $DESC_DIR
    fi
    if [ -e "$DESC_DIR"/PALO*tar.gz ]; then
      echo "palo already exist."
      exit 0
    fi

    echo "Downloading $FILENAME package from $DOWNLOAD_URL to $DESC_DIR"
    wget --no-check-certificate $DOWNLOAD_URL -O $DESC_DIR/$FILENAME.tar.gz
    cd $DESC_DIR
    echo "start to decompress palo package"
    tar -zxf "$FILENAME".tar.gz
    mv PALO* $FILENAME
    echo "file name : $FILENAME"
    echo "start to copy fe be broker to dest dir"
    cp -r $DESC_DIR/$FILENAME/fe $DESC_DIR/
    cp -r $DESC_DIR/$FILENAME/be $DESC_DIR/
    if [ ! -d $DESC_DIR/$FILENAME/apache_hdfs_broker/output ]; then
      cp -r $DESC_DIR/$FILENAME/apache_hdfs_broker $DESC_DIR/
    else
      cp -r $DESC_DIR/$FILENAME/apache_hdfs_broker/output/apache_hdfs_broker $DESC_DIR/
      echo "move broker success"
    fi
    mv $DESC_DIR/apache_hdfs_broker broker

    rm -rf $DESC_DIR/$FILENAME
    return 1
}

echo "download url is $1, dest dir is $2"
download_func $1 $2
if [ "$?"x == "0"x ]; then
  exit 1
fi
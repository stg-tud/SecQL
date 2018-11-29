#!/bin/bash
set -e # Exit on errors

HOST=127.0.0.1
PORT=27017
DB=i3ql-benchmarks
REMOTE_REPO=https://github.com/DSoko2/i3ql-measurements

ID=$(dialog --inputbox "Which measurement ID shall be imported?" 0 0 xxxx --output-fd 1)
REPO=/tmp/i3ql-measurements-$ID

dialog --infobox "Creating local repository" 15 50
if [ -d $REPO ]; then
	dialog --infobox "Temporary repo directory $REPO already exists. Please chose another id or remove it and rerun the script" 0 0
	exit 1
fi
git clone $REMOTE_REPO --branch $ID --single-branch $REPO
cd $REPO

COLLECTIONS=$(ls *.*.*.*.json)
COLLECTIONS=$(dialog --checklist "Which collections shall be imported?" 0 0 0 $(echo $COLLECTIONS | sed -r 's/([^ ]*).json( |$)/\1\n import\non\n/g') --output-fd 1)

EXISTING_COLLECTIONS=$(mongo --host $HOST --port $PORT \
	--eval "db.getCollectionInfos()" \
	$DB | \
grep '"name"\s*:\s*".*\..*\..*\..*"' |
sed -r 's/^.*"name"\s*:\s*"([^"]*)".*$/\1/')

dialog --infobox "Importing collections to repo" 15 50
IMPORTED_COLLECTIONS=
for COLLECTION in $COLLECTIONS
do
	# Check and decide on existing collections that overlap
	SKIP=0
	for EXISTING_COLLECTION in $EXISTING_COLLECTIONS
	do
		if [ $COLLECTION == $EXISTING_COLLECTION ]; then
			if ! dialog --yesno "Collection $COLLECTION exists already. Shall it be replaced? If you select no, import of the collection will be skipped." 15 50; then
				SKIP=1
			fi
		fi
	done
	if [ $SKIP == 0 ]; then
		dialog --infobox "Importing collection $COLLECTION" 15 50
		mongoimport --host $HOST --port $PORT --db $DB --collection $COLLECTION --drop --file $COLLECTION.json
		IMPORTED_COLLECTIONS="$IMPORTED_COLLECTIONS\n$COLLECTION"
	fi
done

dialog --infobox "Cleaned up and done import to $ID:$IMPORTED_COLLECTIONS" 15 50
rm -rf $REPO
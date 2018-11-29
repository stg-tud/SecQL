#!/bin/bash
set -e # Exit on errors

HOST=127.0.0.1
PORT=27017
DB=i3ql-benchmarks
REMOTE_REPO=https://github.com/DSoko2/i3ql-measurements

COLLECTIONS=$(mongo --host $HOST --port $PORT \
	--eval "db.getCollectionInfos()" \
	$DB | \
grep '"name"\s*:\s*".*\..*\..*\..*"' |
sed -r 's/^.*"name"\s*:\s*"([^"]*)".*$/\1/')

COLLECTIONS=$(dialog --checklist "Which collections shall be exported?" 0 0 0 $(echo $COLLECTIONS | sed -r 's/([^ ]*)( |$)/\1\n export\non\n/g') --output-fd 1)
ID=$(dialog --inputbox "Export id" 0 0 $(echo $COLLECTIONS | sed -r 's/[^ .]*\.[^ .]*\.([^ .]*)\..*/\1/') --output-fd 1)
PUSH_NAME=$(dialog --inputbox "The name to use for the git push, your name" 0 0 --output-fd 1)
PUSH_EMAIL=$(dialog --inputbox "The email to use for the git push, your github email" 0 0 --output-fd 1)

REPO=/tmp/i3ql-measurements-$ID

dialog --infobox "Creating local repository" 15 50
if [ -d $REPO ]; then
	dialog --infobox "Temporary repo directory $REPO already exists. Please chose another id or remove it and rerun the script" 0 0
	exit 1
fi
git init $REPO
cd $REPO
git config user.name $PUSH_NAME
git config user.email $PUSH_EMAIL
git checkout -b $ID

dialog --infobox "Exporting collections to repo" 15 50
for COLLECTION in $COLLECTIONS
do
	dialog --infobox "Exporting collections to repo\n\nCollection: $COLLECTION" 15 50
	mongoexport --host $HOST --port $PORT --db $DB --collection $COLLECTION --out $COLLECTION.json
done

dialog --msgbox "Going now to push exported data to remote repo $REMOTE_REPO branch $ID" 15 50
git remote add origin $REMOTE_REPO
set +e # Pull branch, if it already exists, ignore if it doesn't
git pull origin $ID
set -e # Re-enable exit on errors
git commit -m "Measurements $ID via push-measurements.sh"
git push -u origin $ID

dialog --infobox "Cleaned up and done export to $ID:\n$COLLECTIONS" 15 50
rm -rf $REPO
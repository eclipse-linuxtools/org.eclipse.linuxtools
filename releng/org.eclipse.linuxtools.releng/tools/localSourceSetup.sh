#!/bin/bash

# change into tools subdirectory
cd `dirname $0`
buildProperties=../build.properties
#org.eclipse.linuxtools.releng/setup
pushd ../..
workspace=`pwd`
popd
mapfile=../maps/linuxtools.map
dirProp=localSourceCheckoutDir

#need localSourceCheckoutDir
checkoutDir=`grep "^$dirProp" $buildProperties | awk -F= '{ print $2 }'`
if [[ -z "$checkoutDir" ]]; then
  echo "$0: $dirProp not defined in build.properties" >&2
  exit 1
fi

echo "Using $dirProp $checkoutDir"
mkdir -p "$checkoutDir"

echo "Symlinking plugins and fragments"
mkdir -p "$checkoutDir/plugins"
for plugin in `grep -E '^(plugin|fragment).*SVN' $mapfile | awk -F/ '{ print $NF }'`
do
  if [[ -d "$workspace/$plugin" ]]; then
    ln -sfT "$workspace/$plugin" "$checkoutDir/plugins/$plugin"
  else
    echo "Skipping: $plugin"
  fi
done

echo "Symlinking features"
mkdir -p "$checkoutDir/features"
for feature in `grep '^feature.*SVN' $mapfile | awk -F/ '{ print $NF }'`
do
  if [[ -d "$workspace/$feature" ]]; then
    hasFeatureInName=`echo $feature | grep -c -E 'feature$'`
    if [[ $hasFeatureInName -gt 0 ]]; then
      # if .feature, replace with -feature
      fixedName=`echo $feature | sed 's/\.feature$/-feature/'`
    else
      # no "feature", so append
      fixedName="$feature-feature"
    fi
    ln -sfT "$workspace/$feature" "$checkoutDir/features/$fixedName"
  else
    echo "Skipping: $feature"
  fi
done

echo "Moving docs"
mkdir -p "$checkoutDir/doc"
mv -f "$checkoutDir/plugins/"*{.doc,.docs,_doc,_docs}{,.*} "$checkoutDir/doc/" 2>/dev/null

echo "Moving examples"
mkdir -p "$checkoutDir/examples"
mv -f "$checkoutDir/plugins/"*.examples{,.*} "$checkoutDir/examples/" 2>/dev/null

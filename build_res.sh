#!/bin/bash

function is_installed() {
  command -v $1 >/dev/null 2>&1
  return $!
}

function filename() {
     basename $1 .png
}

if $(is_installed identify) && $(is_installed convert)
then

  for image in Swipper2/res/drawable-xxhdpi/*
  do
    file_name=$(filename $image)
    convert $image -background transparent -flatten               PNG32:Swipper2/res/drawable-xxhdpi/$file_name.png
    convert $image -background transparent -flatten -resize 66.6% PNG32:Swipper2/res/drawable-xhdpi/$file_name.png
    convert $image -background transparent -flatten -resize 50.0% PNG32:Swipper2/res/drawable-hdpi/$file_name.png
    convert $image -background transparent -flatten -resize 33.3% PNG32:Swipper2/res/drawable-mdpi/$file_name.png
  done

else
  echo "
    Imagemagick is not installed!
    On Linux:
      $> sudo apt-get imagemagick
    On OSX:
      $> brew install imagemagick
    On Windows:
      Download and install: http://www.imagemagick.org/script/binary-releases.php#windows
  "
fi

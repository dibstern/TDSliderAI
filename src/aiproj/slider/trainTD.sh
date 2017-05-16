#!/bin/bash
mkdir -p bin
javac -d bin/ *.java

COUNTER=0
while [  $COUNTER -lt 500 ]
do
    java -cp bin aiproj.slider.Referee 7 aiproj.slider.TDPlayerThree aiproj.slider.TDPlayerFour 0 -Xmx1500k
    let COUNTER=$COUNTER+1
    echo 'Completed Game:'+$COUNTER
done

rm -r bin
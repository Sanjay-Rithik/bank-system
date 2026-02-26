#!/bin/bash
echo "Building NexBank..."
mkdir -p out
javac -d out src/bank/*.java
if [ $? -eq 0 ]; then
  echo "Build successful! Starting server..."
  echo "Open http://localhost:8080 in your browser."
  java -cp out bank.Server
else
  echo "Build failed."
fi

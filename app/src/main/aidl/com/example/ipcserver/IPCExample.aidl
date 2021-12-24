// IPCExample.aidl
package com.example.ipcserver;

// Declare any non-default types here with import statements

interface IPCExample {

   int getPid();

   int getConnectionCount();

   void setDisplayedValue(String packageName, int pid, String data);
}
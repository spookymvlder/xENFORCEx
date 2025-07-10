package com.spookymvlder.formtools.utilities;

public enum ErrorSeverity {
    NOTIFICATION,           //FYI this could be better
    WARNING,                //This may cause unintended behavior
    ERROR,                  //This will cause issues, but can continue current phase of processing
    FATAL                   //Unable to continue until resolved
}

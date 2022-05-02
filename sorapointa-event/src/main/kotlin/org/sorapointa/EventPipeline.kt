package org.sorapointa

import kotlinx.coroutines.runBlocking


fun main() = runBlocking {

}

class EventPipeline {


}


interface Event

/*

(Cancel)
Data -> Pipe -> Pipe -> Pipe -> Return
     -> (Pipe) Return | Queue
     -> (Pipe) Return | Queue



 */

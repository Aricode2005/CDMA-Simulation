# CDMA Simulation with Walsh Codes

This project is a distributed Java-based simulation of Code Division Multiple Access (CDMA) over a shared network channel. It uses Walsh Codes to perfectly multiplex and demultiplex concurrent data transmissions, mirroring real-world physical airwaves.

## Features
* **Dynamic Walsh Matrix Generation**: Automatically generates orthogonal Walsh code sequences. If the number of stations ($N$) isn't a power of 2, the system automatically sizes up the matrix to the next power of 2 to ensure orthogonality.
* **Distributed Architecture**: Uses a central `ChannelServer` as the multiplexing medium, and individual `Station` clients that act as both Senders and Receivers over TCP sockets.
* **Payload Support**: Send arbitrary text strings or send the contents of a file (`input.txt`).
* **Deep Granular Logging**: 
  * Sender-side text-to-binary transformation mapping.
  * Bit-by-bit spread spectrum chipping visualization.
  * Live channel aggregation (summing of simultaneous voltages).
  * Dot-product calculation and thresholding for decoding at the receiver.
  * Buffer aggregation and character reconstruction.

## Requirements
* Java Development Kit (JDK) 8 or higher.

## How to Compile

Open a terminal in the `Assignment4` directory and run the following command to compile all Java files into an `out` folder:

```powershell
javac -d out src\cdma\*.java
```

## How to Run

Because this is a distributed system, you will need to open **multiple terminals** (one for the server, and one for each station you wish to simulate).

### 1. Start the Channel Server
In your first terminal, start the simulated physical channel medium:
```powershell
java -cp out cdma.ChannelServer
```
* It will ask for the number of stations ($N$). For example, type `4`. 
* It will bind to port `8080` and print the generated Walsh matrix. It will then hang and wait for the stations to connect.

### 2. Start the Stations
Open $N$ additional terminals (e.g., 4 more terminals if $N=4$). In **each** terminal, run:
```powershell
java -cp out cdma.Station
```

### 3. Configure the Simulation
* **IP Address**: Press `Enter` to connect to `localhost`.
* **Payload**: Enter a string (e.g. `Hello!`) or type `FILE` to send the contents of a local `input.txt` file. Leave it empty to act as a silent station.
* **Destination**: Type the Station ID (0 to N-1) of the receiver you want to send your data to.

### 4. Watch the Transmission
Once you hit enter on the last station, the synchronized simulation loop immediately triggers. 
- The **Sender** encodes bits into arrays of chips using the destination's Walsh code.
- The **Channel Server** adds all chip arrays together.
- The **Receiver** performs a dot product on the combined signal against its own Walsh code to decode the original bits and reformulate the payload.

## Project Structure
* `src/cdma/Walsh.java` - Generates the Walsh matrices.
* `src/cdma/ChannelServer.java` - Central socket server handling the synchronization and mathematical multiplexing of the medium.
* `src/cdma/Station.java` - The client terminal combining Sender and Receiver protocols, encoding payloads, and decoding streams.

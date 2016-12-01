package fileMaker;

import data.Link;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by christof on 18.11.16.
 */

/*Deze functie neemt een New York City Taxi Data file, een start- en eindtijd, een kaart en een
 positie->wegen-map als input en produceert een .CSV-file met pickuptimes, ophaal- en afzetlocaties .*/

public class PassengerFileMaker {


    private IOHandler ioHandler;


    public PassengerFileMaker(IOHandler ioHandler) throws IOException, ClassNotFoundException {
        this.ioHandler = ioHandler;
//        positionToClosestLinksMap = Link.getPositionToClosestLinksMap(ptclFileName);
    };

    public void extractAndPositionPassengers() throws IOException, ClassNotFoundException {
        Extractor extractor= new Extractor(getIoHandler());
        extractor.extractAndPositionPassengers();
    }

    public IOHandler getIoHandler() {
        return ioHandler;
    }
}

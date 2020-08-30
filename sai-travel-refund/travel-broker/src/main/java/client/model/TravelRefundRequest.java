package client.model;

/**
 *
 * This class stores all information about a request that a client submits to
 * get a travel refund request.
 *
 */
public class TravelRefundRequest {

    private Address origin;
    private Address destination;
    private String teacher;
    private String student;
    private double costs;
    private int kilometers;
    private ClientTravelMode mode;

    public TravelRefundRequest() {
    }


    public TravelRefundRequest(Address origin, Address destination, String teacher, String student, double costs) {
        this.origin = origin;
        this.destination = destination;
        this.teacher = teacher;
        this.student = student;
        this.mode = ClientTravelMode.PUBLIC_TRANSPORT;
        this.costs = costs;
        this.kilometers = 0;
    }

    public TravelRefundRequest(Address origin, Address destination, String teacher, String student, int kilometers) {
        this.origin = origin;
        this.destination = destination;
        this.teacher = teacher;
        this.student = student;
        this.mode = ClientTravelMode.CAR;
        this.costs = 0;
        this.kilometers = kilometers;
    }

    public Address getOrigin() {
        return origin;
    }

    public void setOrigin(Address origin) {
        this.origin = origin;
    }

    public Address getDestination() {
        return destination;
    }

    public void setDestination(Address destination) {
        this.destination = destination;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }


    public double getCosts() {
        return costs;
    }

    public void setCosts(double costs) {
        this.costs = costs;
        if (costs <= 0) {
            mode = ClientTravelMode.CAR;
        }

    }

    public int getKilometers() {
        return kilometers;
    }

    public void setKilometers(int kilometers) {
        this.kilometers = kilometers;
    }

    @Override
    public String toString() {
        return teacher + "-" + student + "-" + origin + "-" + destination +"-" +mode+"-"+costs+"e-"+kilometers+"km";
    }

    public ClientTravelMode getMode() {
        return mode;
    }

    public void setMode(ClientTravelMode mode) {
        if (mode == ClientTravelMode.CAR) {
            this.costs = 0;
        }
        if (mode == ClientTravelMode.PUBLIC_TRANSPORT){
            this.kilometers = 0;
        }
        this.mode = mode;
    }
}

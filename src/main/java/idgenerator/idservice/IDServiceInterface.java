package idgenerator.idservice;

public interface IDServiceInterface {

    long getUnusedId();

    void clearIdStore();
}

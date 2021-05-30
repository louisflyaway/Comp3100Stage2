//44434065 Runde Jia
public class Server{
    
	//Collect server information
	public int id;
	public String type;
	public int limit;
	public int bootupTime;
	public float hourlyRate;
	public int coreCount;
	public int memory;
	public int disk;
	public int state;
	public int availableTime;
	private static int shortDefiniteTime;

       Server(int id, String type, int limit, int bootupTime, float hourlyRate, int coreCount, int memory, int disk){
    	   this.id = id;
    	   this.type = type;
    	   this.limit = limit;
    	   this.bootupTime = bootupTime;
    	   this.hourlyRate = hourlyRate;
    	   this.coreCount = coreCount;
    	   this.memory = memory;
    	   this.disk = disk;
       }
       
       Server(String tipe, int id, int state, int availTime, int core, int mem, int disk) {
   		this.type = tipe;
   		this.id = id;
   		this.state = state;
   		this.availableTime = availTime;
   		this.coreCount = core;
   		this.memory = mem;
   		this.disk = disk;
   	}
	   

    public static void setShortDefiniteTime(int value) {
        shortDefiniteTime = value;
    }

    // we were told that this is a server with its available time
    // equal to the submission time of the job.
    // however, I think it must be LESS THAN OR EQUAL
    public boolean isImmediatelyAvailable(Job job) {
        return (availableTime <= job.getSubitTime());
    }

    // server is available in short definite time:
    // a server that can execute within a fixed amount of time;
    // note the available time of an Active server with insufficient available resources
    // is unknown as run time(s) of running jobs are the only estimate; hence, unknown.
    public boolean isAvailableInShortDefiniteTime() {
        return (availableTime <= shortDefiniteTime);
    }

    public boolean isActive() {
        return (state == 3);
    }

    public int getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(int time) {
        this.availableTime = time;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getBootupTime() {
        return bootupTime;
    }

    public void setBootupTime(int bootupTime) {
        this.bootupTime = bootupTime;
    }

    public Float getRate() {
        return hourlyRate;
    }

    public void setRate(int rate) {
        this.hourlyRate = rate;
    }

    public int getCoreCount() {
        return coreCount;
    }

    public void setCoreCount(int coreCount) {
        this.coreCount = coreCount;
    }

    public int getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public int getDisk() {
        return disk;
    }

    public void setDisk(int disk) {
        this.disk = disk;
    }

}

package com.wukong.mapreduce.version;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author fan
 */
public class VersionInfoWritable extends VersionInfo
        implements WritableComparable<VersionInfoWritable> {

    public void write(DataOutput out) throws IOException{
        out.writeUTF(getDate());
        out.writeUTF(getUser());
        out.writeUTF(getGame());
        out.writeInt(getHour());
        out.writeUTF(getFrom());
        out.writeUTF(getVersion());
        out.writeUTF(getLocation());
    }

    public VersionInfoWritable(){
        super();
    }

    public VersionInfoWritable(String date, String user, String game, int hour, String from, String version, String location) {
        super(date, user, game, hour, from, version, location);
    }

    public void readFields(DataInput in) throws IOException{
        setDate(in.readUTF());
        setUser(in.readUTF());
        setGame(in.readUTF());
        setHour(in.readInt());
        setFrom(in.readUTF());
        setVersion(in.readUTF());
        setLocation(in.readUTF());
    }

    public int compareTo(VersionInfoWritable o){
        //最先按照人名排序
        if(this.getUser().equalsIgnoreCase(o.getUser())){
            //先按照时间排序
            if(this.getDate().equalsIgnoreCase(o.getDate())){
                //再次按照时间排序
                if(this.getHour()==o.getHour()){
                    return 0;
                }else{
                    return this.getHour()-o.getHour();
                }

            }else{
                return this.getDate().compareTo(o.getDate());
            }

        }else{
            return this.getUser().compareTo(o.getUser());
        }

    }

}

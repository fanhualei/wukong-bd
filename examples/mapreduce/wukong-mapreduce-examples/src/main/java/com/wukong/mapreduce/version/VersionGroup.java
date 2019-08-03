package com.wukong.mapreduce.version;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

public class VersionGroup extends WritableComparator {
    public VersionGroup(){
        super(VersionInfoWritable.class,true);
    }
    @Override
    public int compare(WritableComparable a, WritableComparable b){
        VersionInfoWritable s1=(VersionInfoWritable)a;
        VersionInfoWritable s2=(VersionInfoWritable)b;
        return s1.getUser().compareTo(s2.getUser());
    }
}

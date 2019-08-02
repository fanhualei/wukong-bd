package com.wukong.mapreduce.scorePro;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * 分组器
 * 分组对象
 */
public class MyGroup extends WritableComparator {


    public MyGroup() {
        //创建对象
        super(Student.class, true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {

        Student s1 = (Student) a;
        Student s2 = (Student) b;
        //设置课程分组器
        return s1.getCourse().compareTo(s2.getCourse());
    }

}

package com.wukong.mapreduce.scorePro;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.hadoop.io.WritableComparable;

public class Student implements WritableComparable<Student>{
    private String name;
    private float score;
    private String course;


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public float getScore() {
        return score;
    }
    public void setScore(float score) {
        this.score = score;
    }
    public String getCourse() {
        return course;
    }
    public void setCourse(String course) {
        this.course = course;
    }

    @Override
    public String toString() {
        DecimalFormat fs = new DecimalFormat("#.#");
        return  course + "\t" +name+ "\t"+ fs.format(score);
    }

    public Student() {

    }


    public Student(String name, float score, String course) {
        super();
        this.name = name;
        this.score = score;
        this.course = course;
    }

    public int compareTo(Student o) {
        int diff = this.course.compareTo(o.course);
        if (diff == 0) {

            return (int)(o.score - this.score);
        }else{
            return diff > 0 ? 1 : -1;
        }
    }


    public void readFields(DataInput in) throws IOException {
        name = in.readUTF();
        score = in.readFloat();
        course = in.readUTF();
    }

    public void write(DataOutput out) throws IOException {
        out.writeUTF(name);
        out.writeFloat(score);
        out.writeUTF(course);
    }


}

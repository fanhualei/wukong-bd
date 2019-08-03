package com.wukong.mapreduce.scorePro;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.File;
import java.io.IOException;

/**
 * 求每个科目中分数最高的学生,select max(avg) from score  group by 科目
 * @author fan
 */
public class CourseHighestScoreStudent {


    private static class MyMapper extends Mapper<Object,Text,Student,NullWritable>{
        @Override
        public void map(Object inKey,Text inValue,Context context)
                throws IOException,InterruptedException{
            //计算机,黄晓明,85,86,41,75,93,42,85
            String fields[]=inValue.toString().split(",");
            int sum=0,count=0;
            float avg;
            for(int j=2;j<fields.length;j++){
                count++;
                sum+=Integer.parseInt(fields[j]);
            }
            avg = sum/count;
            context.write(new Student(fields[1],avg,fields[0]),NullWritable.get());
        }
    }

    private static  class MyReduce extends Reducer<Student,NullWritable,Student,NullWritable>{

        @Override
        public void reduce(Student student,Iterable<NullWritable> inValues,Context context)
            throws IOException,InterruptedException{
            context.write(student,NullWritable.get());
        }
    }


    public static void main(String[] args) throws Exception{

        //提前删除输出目录
        File outPutDir=new File(args[1]);
        if(outPutDir.exists()){
            FileUtils.deleteDirectory(outPutDir);
        }


        Configuration conf=new Configuration();
        Job job=Job.getInstance(conf,"求一个科目中最高分数的学生");

        job.setJarByClass(CourseHighestScoreStudent.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReduce.class);

        job.setMapOutputKeyClass(Student.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setOutputKeyClass(Student.class);
        job.setOutputValueClass(NullWritable.class);


        //调用分组
        job.setGroupingComparatorClass(MyGroup.class);



        FileInputFormat.addInputPath(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job,new Path(args[1]));

        System.exit(job.waitForCompletion(true)?0:1);

    }





}

package com.wukong.mapreduce.scorePro;


import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.File;
import java.io.IOException;

/**
 * 将不同课程的成绩导出到不同的文件中,并将学生的成绩按照倒序进行排序.
 * 使用了Partitioner进行分页
 * @author fan
 */
public class CourseScoreAverageProPartion {





    private static class MyPartition extends  Partitioner<Student, NullWritable>{
        @Override
        public int getPartition(Student key, NullWritable value, int numReduceTasks) {

            String course=key.getCourse();
            if(course.equals("计算机")){
                return 0;
            }else if(course.equals("英语")){
                return 1;
            }else if(course.equals("语文")){
                return 2;
            }else if(course.equals("数学")){
                return 3;
            }
            return 100;
        }
    }



    private static class MyMapper extends Mapper<Object,Text,Student,NullWritable>{
        @Override
        public void map(Object inKey,Text inValue,Context context)
                throws IOException,InterruptedException{

            //输入数据:计算机,黄晓明,85,86,41,75,93,42,85
            String[] fields=inValue.toString().split(",");
            int count=0,sum=0;
            float avg;
            for(int i=2;i<fields.length;i++){
                count++;
                sum+=Integer.parseInt(fields[i]);
            }
            avg=sum/count;
            context.write(new Student(fields[1],avg,fields[0]),NullWritable.get());
        }
    }


    private static  class  MyReducer extends Reducer<Student,NullWritable,Student,NullWritable>{
        @Override
        public void reduce(Student inKey, Iterable<NullWritable> inValues,Context context) throws IOException,InterruptedException{
            context.write(inKey,NullWritable.get());

        }

    }


    public static void main(String[] args) throws Exception{

        //提前删除输出目录
        File outPutDir=new File(args[1]);
        if(outPutDir.exists()){
            FileUtils.deleteDirectory(outPutDir);
        }


        Configuration conf=new Configuration();
        Job job=Job.getInstance(conf,"将不同课程的成绩导出到不同的文件中,并将学生的成绩按照倒序进行排序.");

        job.setJarByClass(CourseScoreAverageProPartion.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        job.setMapOutputKeyClass(Student.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        FileInputFormat.addInputPath(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job,new Path(args[1]));


        job.setPartitionerClass(MyPartition.class);
        job.setNumReduceTasks(4);


        System.exit(job.waitForCompletion(true)?0:1);


    }



}

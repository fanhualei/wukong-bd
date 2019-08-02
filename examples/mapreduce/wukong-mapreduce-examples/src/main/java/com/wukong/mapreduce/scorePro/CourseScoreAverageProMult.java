package com.wukong.mapreduce.scorePro;


import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

import java.io.File;
import java.io.IOException;

/**
 * 将不同课程的成绩导出到不同的文件中,并将学生的成绩按照倒序进行排序.
 * 使用 MultipleOutputs 来生成文件
 * @author fan
 */
public class CourseScoreAverageProMult  {





    private static class MyMapper extends Mapper<Object,Text,Student,NullWritable>{

        @Override
        public void map(Object inKey,Text inValue,Context context)
                throws IOException,InterruptedException{

            //输入数据:计算机,黄晓明,85,86,41,75,93,42,85
            String[] fields=inValue.toString().split(",");

            int count=0;
            int sum=0;
            float avg;
            for(int i=2;i<fields.length;i++){
                count++;
                sum+=Integer.parseInt(fields[i]);
            }
            avg=sum/count;
            Student student=new Student(fields[1],avg,fields[0]);
            context.write(student,NullWritable.get() );

        }

    }


    private static  class  MyReducer extends Reducer<Student,NullWritable,Student,NullWritable>{
        @Override
        public void reduce(Student student, Iterable<NullWritable> inValues,Context context) throws IOException,InterruptedException{
            multipleOutputs.write(student,NullWritable.get(),student.getCourse());
        }


        /**
         * 文件输出
         */
        private MultipleOutputs<Student,NullWritable> multipleOutputs;
        @Override
        protected void setup(Context context){
            multipleOutputs = new MultipleOutputs<Student,NullWritable>(context);
        }
        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException{
            multipleOutputs.close();
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

        job.setJarByClass(CourseScoreAverageProMult.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        job.setMapOutputKeyClass(Student.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setOutputKeyClass(Student.class);
        job.setOutputValueClass(NullWritable.class);

        FileInputFormat.addInputPath(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job,new Path(args[1]));

        System.exit(job.waitForCompletion(true)?0:1);


    }



}

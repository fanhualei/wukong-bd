package com.wukong.mapreduce.scorePro;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * 将不同课程的成绩导出到不同的文件中,并将学生的成绩按照倒序进行排序.
 * 使用了Partitioner进行分页
 * @author fan
 */
public class CourseScoreAverageProPartion {





    private static class MyPartition extends  Partitioner<Text, FloatWritable>{
        @Override
        public int getPartition(Text key, FloatWritable value, int numReduceTasks) {
            String[] fields=key.toString().split(",");

            if(fields[0].equals("计算机")){
                return 0;
            }else if(fields[0].equals("英语")){
                return 1;
            }else if(fields[0].equals("语文")){
                return 2;
            }else if(fields[0].equals("数学")){
                return 3;
            }
            return 100;

        }
    }



    private static class MyMapper extends Mapper<Object,Text,Text,FloatWritable>{

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
            context.write(new Text(fields[0]+","+fields[1]),
                    new FloatWritable(avg)
                    );

        }

    }


    private static  class  MyReducer extends Reducer<Text,FloatWritable,Text,FloatWritable>{
        @Override
        public void reduce(Text inKey, Iterable<FloatWritable> inValues,Context context) throws IOException,InterruptedException{

            FloatWritable value=inValues.iterator().next();
            context.write(inKey,value);

        }

    }


    public static void main(String[] args) throws Exception{
        Configuration conf=new Configuration();
        Job job=Job.getInstance(conf,"将不同课程的成绩导出到不同的文件中,并将学生的成绩按照倒序进行排序.");

        job.setJarByClass(CourseScoreAverageProPartion.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        job.setMapOutputValueClass(FloatWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(FloatWritable.class);

        FileInputFormat.addInputPath(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job,new Path(args[1]));


        job.setPartitionerClass(MyPartition.class);
        job.setNumReduceTasks(4);


        System.exit(job.waitForCompletion(true)?0:1);


    }



}

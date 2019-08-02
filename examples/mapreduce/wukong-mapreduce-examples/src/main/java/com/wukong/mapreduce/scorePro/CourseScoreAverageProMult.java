package com.wukong.mapreduce.scorePro;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

import java.io.IOException;

/**
 * 将不同课程的成绩导出到不同的文件中,并将学生的成绩按照倒序进行排序.
 * 使用 MultipleOutputs 来生成文件
 * @author fan
 */
public class CourseScoreAverageProMult  {





    private static class MyMapper extends Mapper<Object,Text,Text,FloatWritable>{

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
            context.write(new Text(fields[0]+","+fields[1]),
                    new FloatWritable(avg)
                    );

        }

    }


    private static  class  MyReducer extends Reducer<Text,FloatWritable,Text,FloatWritable>{
        @Override
        public void reduce(Text inKey, Iterable<FloatWritable> inValues,Context context) throws IOException,InterruptedException{

            FloatWritable value=inValues.iterator().next();
            String[] fileds=inKey.toString().split(",");
            multipleOutputs.write(inKey,value,fileds[0]);

        }


        private String getName(String inStr){
            if(inStr.equals("计算机")){
                return "aaa";
            }else if(inStr.equals("英语")){
                return "bbb";
            }else if(inStr.equals("语文")){
                return "ccc";
            }else if(inStr.equals("数学")){
                return "ddd";
            }
            return "eee";
        }


        /**
         * 文件输出
         */
        private MultipleOutputs<Text,FloatWritable> multipleOutputs;
        @Override
        protected void setup(Context context){
            multipleOutputs = new MultipleOutputs<Text,FloatWritable>(context);
        }
        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException{
            multipleOutputs.close();
        }

    }


    public static void main(String[] args) throws Exception{
        Configuration conf=new Configuration();
        Job job=Job.getInstance(conf,"将不同课程的成绩导出到不同的文件中,并将学生的成绩按照倒序进行排序.");

        job.setJarByClass(CourseScoreAverageProMult.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        job.setMapOutputValueClass(FloatWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(FloatWritable.class);

        FileInputFormat.addInputPath(job,new Path(args[0]));
        FileOutputFormat.setOutputPath(job,new Path(args[1]));




        System.exit(job.waitForCompletion(true)?0:1);


    }



}

# hadoop官方的例子


从hadoop2.9.2-src包中精简的代码.具体步骤如下

1:新建一个目录hadoop-examples
2:将hadoop-src目录下的:pom.xml hadoop-project  hadoop-mapreduce-project/hadoop-mapreduce-examples,复制过来.
3:用idea打开并进行编译.
  3.1:在编译的过程中,要使用jdk1.8,因为hadoop-annotations2.9.2使用了这个包.
  具体在idea-file-project structre-project菜单中,选择jdk8
4:在
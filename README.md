Farabi
======

Mass Signal Extraction & Analysis Architecture based On Distrubuted System

### Install
```bash
git clone https://github.com/bahadrix/Farabi.git Farabi
cd Farabi
mvn package
```

### Creating Work Set Package From Existing MP3 Files

We use the CreatePack job for creating the HDFS package.

####Usage
```bash
me.farabi.job.CreatePack [opts] <local_dir> <hdfs_dir>
   [opts]
       -m <num>    : Maximum number of files to be processed.
```

####Examples
Create package on HDFS from first 200 file in ~/mp3 folder.

```bash
hadoop jar target/Farabi-1.0-SNAPSHOT.jar me.farabi.job.CreatePack ~/mp3 farabi/input -m 200
```
-m argument defines maximum number of files used from source directory.

We can create that package from all files in that directory like this:
```bash
hadoop jar target/Farabi-1.0-SNAPSHOT.jar me.farabi.job.CreatePack ~/mp3 farabi/input
```


### Running Job on a Package
#### Job Information File
farabi.properties file must be defined like this:
```INI
mongodb.server.host=mongodb.host.address
mongodb.server.port=27017
mongodb.db=farabi
```

####Usage
```bash
MongoSone <input> <output> [-p <properties file>]
   <input>                 : Package data file location on HDFS
   <output>                : Output location on HDFS for logs and stuff
   -p <properties file>    : Properties file for mongodb connection info and stuff.
                             Default: farabi.properties
```

####Example
```bash
hadoop jar target/Farabi-1.0-SNAPSHOT.jar \
  me.farabi.job.MongoSone \
  farabi/input/all/data farabi/output \
  -p target/classes/farabi.sample.properties
```

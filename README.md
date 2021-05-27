# nbody
nbody simulation


## Requirements

* Java >= 1.8

## How To Run

In the example below we are going to use the following setup:

* 2 cluster nodes/servers
* 2 nbody nodes
* 4 bodies


1. Download [parapet-cluster](https://s3.amazonaws.com/parapet.io/cluster-distribution/parapet-cluster-0.0.1-RC5.tgz)
2.Set up at least two nodes. Below you will find configs for both servers.

`server-1` port = `5555`

`node.properties`:

```
node.id=server-1
node.address=localhost:5555
node.peers=server-2:localhost:5556
node.election-delay=10
node.heartbeat-delay=5
node.monitor-delay=10
node.peer-timeout=10
node.leader-election-threshold=1
```

`server-2` port = `5556`

`node.properties`:

```
node.id=server-2
node.address=localhost:5556
node.peers=server-1:localhost:5555
node.election-delay=10
node.heartbeat-delay=5
node.monitor-delay=10
node.peer-timeout=10
node.leader-election-threshold=0.3
```

3. Run servers:
  * Go to parapet-cluster-{VERSION} and run `./bin/cluster`. _Note_: there is a bug when running from `bin` folder. It will be fixed in the future release.
  * Wait until you see `current leader: 'localhost:{port}' is healthy` in one of the servers logs

For a more detailed guide, please [read#network](http://parapet.io/)


4. From the project folder: `sbt "project api" "protobufGenerate"`
5. From the project folder: `sbt "project node" "universal:packageBin"`, target: `./node/target/universal/node-0.1.0-SNAPSHOT.zip` 
6. From the project folder: `sbt "project coordinator" "universal:packageBin"`, target: `./coordinator/target/universal/coordinator-0.1.0-SNAPSHOT.zip`
7. Uzip and run `coordinator` from `coordinator-0.1.0-SNAPSHOT/bin`: 
`./coordinator --servers localhost:5555,localhost:5556 --host localhost --port 5001 --nbody-size 4 --node-size 2`
9. Create a data folder data contains initial body data
```
4.84143144246472090e+00,-1.16032004402742839e+00,-1.03622044471123109e-01,1.66007664274403694e-03,7.69901118419740425e-03,-6.90460016972063023e-05,9.54791938424326609e-04
8.34336671824457987e+00,4.12479856412430479e+00,-4.03523417114321381e-01,-2.76742510726862411e-03,4.99852801234917238e-03,2.30417297573763929e-05,2.85885980666130812e-04
1.28943695621391310e+01,-1.51111514016986312e+01,-2.23307578892655734e-01,2.96460137564761618e-03,2.37847173959480950e-03,-2.96589568540237556e-05,4.36624404335156298e-05
1.53796971148509165e+01,-2.59193146099879641e+01,1.79258772950371181e-01,2.68067772490389322e-03,1.62824170038242295e-03,-9.51592254519715870e-05,5.15138902046611451e-05
```
9. Unzip and run `node-1` from `node-0.1.0-SNAPSHOT/bin`:
`./node --servers localhost:5555,localhost:5556 --node-id 1 --host localhost --port 5002 --nbody-size 4 --from 0 --to 2 --data-file {path_to_data}`

10. Unzip and run `node-2` from `node-0.1.0-SNAPSHOT/bin`:
`./node --servers localhost:5555,localhost:5556 --node-id 2 --host localhost --port 5003 --nbody-size 4 --from 2 --to 4 --data-file {path_to_data}`

After some time you should see the following lines in coordinator's logs:

```
2021-05-27 02:51:48 INFO  CoordinatorProcess:91 - round=0
2021-05-27 02:51:48 INFO  CoordinatorProcess:99 - i=0, x=4.8596212813964135, y=-1.0759603701020475, z=-0.104378599298987, vx=0.6063279826922536, vy=2.8119890945320254, vz=-0.02521849422951191, mass=0.03769367487038949
2021-05-27 02:51:48 INFO  CoordinatorProcess:99 - i=1, x=8.313043328051561, y=4.179568195250542, z=-0.4032709305169109, vx=-1.010779666476453, vy=1.8256543427769834, vz=0.0084162203149235, mass=0.011286326131968767
2021-05-27 02:51:48 INFO  CoordinatorProcess:99 - i=2, x=12.926853269578215, y=-15.08508996939484, z=-0.2236325576838558, vx=1.0827902500917914, vy=0.8687143968148111, vz=-0.010832623268735542, mass=0.0017237240570597112
2021-05-27 02:51:48 INFO  CoordinatorProcess:99 - i=3, x=15.409069829850724, y=-25.901473621992622, z=0.17821609402577787, vx=0.9790904702457756, vy=0.5946997290274871, vz=-0.03475596827050346, mass=0.0020336868699246304
2021-05-27 02:51:48 INFO  CoordinatorProcess:91 - round=1
2021-05-27 02:51:48 INFO  CoordinatorProcess:99 - i=0, x=4.877811168499404, y=-0.9916006273526462, z=-0.10513515818820851, vx=0.606329588259012, vy=2.81199138886993, vz=-0.025218629607395054, mass=0.03769367487038949
2021-05-27 02:51:48 INFO  CoordinatorProcess:99 - i=1, x=8.282719776522194, y=4.2343375809295525, z=-0.4030184298543105, vx=-1.0107850443184012, vy=1.825646161322582, vz=0.008416689151424194, mass=0.011286326131968767
2021-05-27 02:51:48 INFO  CoordinatorProcess:99 - i=2, x=12.959336954509885, y=-15.059028496003751, z=-0.22395753605540053, vx=1.0827894996046157, vy=0.86871576605777, vz=-0.010832609286706561, mass=0.0017237240570597112
2021-05-27 02:51:48 INFO  CoordinatorProcess:99 - i=3, x=15.438442537027678, y=-25.883632612162014, z=0.17717341472005452, vx=0.9790902095181364, vy=0.59470045662231, vz=-0.034755980956967684, mass=0.0020336868699246304
2021-05-27 02:51:48 INFO  CoordinatorProcess:91 - round=2
2021-05-27 02:51:48 INFO  CoordinatorProcess:99 - i=0, x=4.896001104252988, y=-0.9072408144035241, z=-0.10589172122542505, vx=0.606331209803856, vy=2.811993729062852, vz=-0.025218767873211475, mass=0.03769367487038949
2021-05-27 02:51:48 INFO  CoordinatorProcess:99 - i=1, x=8.252396061917743, y=4.289106716450975, z=-0.4027659148210066, vx=-1.0107904801188234, vy=1.8256378228558379, vz=0.008417168171739868, mass=0.011286326131968767
2021-05-27 02:51:48 INFO  CoordinatorProcess:99 - i=2, x=12.991820617107802, y=-15.032966981805098, z=-0.2242825140117388, vx=1.0827887549096913, vy=0.8687171259774712, vz=-0.010832595452988677, mass=0.0017237240570597112
2021-05-27 02:51:48 INFO  CoordinatorProcess:99 - i=3, x=15.467815236419593, y=-25.86579158057922, z=0.17613073503558369, vx=0.9790899500512231, vy=0.5947011814483631, vz=-0.03475599356408805, mass=0.0020336868699246304

...
```

Output from the original java [program](https://benchmarksgame-team.pages.debian.net/benchmarksgame/program/nbody-java-1.html):

```
i=0, x=-0.000004, y=-0.000033, z=0.000000, vx=-0.000372, vy=-0.003278, vz=0.000024, mass=39.478418
i=1, x=4.847340, y=-1.132163, z=-0.103871, vx=0.590849, vy=2.815699, vz=-0.024887, mass=0.037694
i=2, x=8.333218, y=4.143035, z=-0.403437, vx=-1.014853, vy=1.823640, vz=0.008613, mass=0.011286
i=3, x=12.905191, y=-15.102457, z=-0.223416, vx=1.082141, vy=0.869475, vz=-0.010821, mass=0.001724
i=4, x=15.389486, y=-25.913364, z=0.178911, vx=0.978869, vy=0.595073, vz=-0.034759, mass=0.002034
```

## Links:

* https://gereshes.com/2018/05/07/what-is-the-n-body-problem
* https://benchmarksgame-team.pages.debian.net/benchmarksgame/program/nbody-java-1.html


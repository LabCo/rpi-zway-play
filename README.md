# Creating you own home automation system as a web developer

As a web developer I have been spoiled by the ease of heroku and docker AWS ECS deploys: fast, easy, and magic. My most recent project took me into the realms of home automation and Raspberry Pi. The standard tutorials with scp-ing builds onto the Pi board seemed archaic and not scalable, the very first step was to figure out a sensible deploy process so I can update the code over air while sitting at home in my pajamas. A coworker suggested chef but after some attempt that turned out to be quite confusing and the documentation was not very clear. Some internet searches later I found http://resin.io, it turned out to be exactly what I needed. 

Resin.io provides heroku style git repository deployments to IoT devices. At the core your hardware is running a modified Linux OS that runs the code in a docker container. The set up process is only a few steps.

1. create a resin project
2. download the os
3. burn os onto sd card
4. put sd card into your board
5. now the system boots up, download you lates build and runs it

For details start up instructions take a look at their doc https://docs.resin.io/raspberrypi3/nodejs/getting-started/

If that was it for setting up a home automation system, this post would not exists, so lets look at the specifics. For my set up, I went ZWave due to the large number ot existing devices I could easily buy. Annoyingly ZWave is proprietary it is not as developer friendly, but pretty much every home automation production is proprietary at least ZWave has everything I need from power meters to lights. 

For the controller I went with ZWay and the Razberry board (razberry.z-wave.me) becasue the controller is fast, has an api, and is light weight leaving enough room to run my own code on the raspberry pi. There is the open zwave project but that has problems with encrypted zwave devices and had to be integrated into something. At the time of my investigation OpenHab 2 was still unstable and slow. I did not find out about home assistant until after I already settled on ZWay, so I did not look at it much, but it also uses open zway and would suffer from the same encrypted device support issues.

# Resin.io with the Play Framework and ZWay

The resin set up had a lot of small problems getting zway to work but it turned out extremely useful afterwards. I decided to share my findings so anybody else can spend more time developing instead of fighting the setup. 

As a big fan of the play framework and scala, so that is what I ran or my server. It fit pretty well and if I do start running into resource issues later there are more powerful Pi compatible board out there. 

The entire set up could bie split up into 3 stages
1. Creating the play server
2. Creating the repository to push into resin
3. Putting the play service distribution into the resin repository
4. Configure Resin to support the serial port for RazBerry

## Play Framework server set up

start with a new play-scala project
  activator new

create a ZWayEvent model to parse the Json coming in from the post https://github.com/LabCo/rpi-zway-play/blob/master/app/models/ZWayEvent.scala

create a helper to write i18n strings to json, not really necessary for such a small set up but ApiController code requires it and it is useful later https://github.com/LabCo/rpi-zway-play/blob/master/app/play/api/libs/json/RichJsError.scala

create an ApiController.scala to handle the json parsing https://github.com/LabCo/rpi-zway-play/blob/master/app/controllers/api/ApiController.scala

create an EventsController.scala so we can parse the updates that come from zway https://github.com/LabCo/rpi-zway-play/blob/master/app/controllers/api/EventsController.scala
 

The play set up is done, run "activator run" and check that is starts up. 

Package the play application with "activator dist", this will create a packaged zip file in target/universal, remember this.


## Resin distribution repository setup

Resin works by pushing your code into a repository, then building a docker image out of it, and pushing the image onto all your hardware devices connected to your resin project. Compiling Scala (and webpack packaging, which which I use on my system) is a resource intense operation and it is a lot faster to build a distribution locally and then push out the packaged distribution. For this reason, I have a separate repository dedicated to the resin distribution.

Create a new repository, I added '-DIST' to the name of the Play app repository, so I have "rpi-zway-play-DIST"

I placed a sample working distribution into github, use that to follow the next steps https://github.com/LabCo/rpi-zway-play-DIST

1. Copy over .gitignore

2. Copy over .dockerignore

3. Copy over 'modules' folder. This has the custom zway module the sents zwave events to our play server

4. Copy over 'nginx' folder, it configures nginx with gzip and routes '/expert*', '/smarthome*/', 'ZAutomation', 'ZWave' urls to the zway server and everything else is routed to the play server 

5. Copy over Dockerfile.template, as required by resin. It should be a pretty clear docker file. I tried to document each of the lines. Its a standard docker file with a resin environment variable. The resin environment converts the template into a Dockerfile on its end. Here are are more details on the templates https://docs.resin.io/deployment/docker-templates/.

6. Copy over the 'start.sh' script, it runs all the setup that has to be done after the box has started because the '/data' persistance storage mount is only available after start, if you try writing to it in the docker file, the stuff will be dropped.


## Deployment

Make sure resin.io's remote is configured

Create a distribution of "rpi-zway-play" 
  $> activator dist

Copy the distribution from target/universal/rpi-zway-play-LATEST_VERSION.zip into the dist repository

Unzip the file into and move it to rpi-zway-play file to replace the old version

Add the new code
  $> git add rpi-zway-play

Commit the code and push it to the repo
  $> git commit -am "updated release"
  $> git push

Release to resin
  $> git push resin master  
  

## Resin Configuration

Because the razberry runs over the serial port over the gpio slots we need to enable 2 environment variables under "Fleet Configuration"

	RESIN_HOST_CONFIG_dtoverlay	= pi3-miniuart-bt
	RESIN_HOST_CONFIG_force_turbo = 1

# Final Words

And that is is, you can go to the resin's console and see your device downloading the code and then running it. Resin will tell you the ip address.

Visit `IP_ADDRESS/api/v1/events` to see events from zway.

Visit `IP_ADDRESS:8083` for the zway interface

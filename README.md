# festivals.ie
A [Grails website](http://festivals.ie) that provides information about festivals in Ireland and beyond.


# Run Locally
If you wish to run the application locally, you must have access to a MySQL server and [the 
relevant Grails version](https://github.com/domurtag/festivals/blob/master/application.properties).

## Secret Configuration
Additionally, a config file containing various sensitive parameters should be added to the 
[conf](https://github.com/domurtag/festivals/tree/master/grails-app/conf) directory, before the application is built.
The name of this file must be `secret.properties`. You can provide environment-specific overrides for these settings by
adding a file named for the environment. For example, to override some/all of the settings in `secret.properties` for
the production environment add these settings to a file named `secret-PRODUCTION.properties` in the same directory. 

The contents of this configuration file are described in the following subsections

### Mandatory Secret Configuration

These settings described must be provided or the application will fail to start

````
dataSource.username=festival
dataSource.password=changeme
````

### Optional Secret Configuration

If these settings are omitted, the application will start, but certain features will not work correctly. The consequences
of omitting each one and (where applicable) how to remove this feature completely are described below

#### Google Maps API Keys

These API keys are used when calling Google Maps APIs on the server and client side.
The hosts/IP addresses on which these keys can be used are [configured here](https://code.google.com/apis/console). 

````
festival.googleApiServerKey=changeme
festival.googleApiClientKey=changeme
````

#### Skiddle API Key

A daily job automatically imports festivals into the database from the [http://www.skiddle.com/api/](Skiddle events API).
This job will fail unless the key below is provided.

````
festival.skiddle.tag=changeme
````

To disable this feature, simply remove the relevant [Quartz class](https://github.com/domurtag/festivals/blob/master/grails-app/jobs/ie/festivals/job/ImportSkiddleFeedJob.groovy).

#### Eventbrite API Key

A daily job automatically imports festivals into the database from the [http://developer.eventbrite.com/](Eventbrite web service).
This job will fail unless the key below is provided.

````
festival.eventbrite.accessToken=changeme
````

To disable this feature, simply remove the relevant [Quartz class](https://github.com/domurtag/festivals/blob/master/grails-app/jobs/ie/festivals/job/ImportEventbriteFestivalsJob.groovy).

#### Muzu API Key

Artist videos are retrieved from [Muzu's Data API](http://www.muzu.tv/api/). 

````
festival.muzuApiKey=changeme
````

To disable this feature, remove the [artist video service](https://github.com/domurtag/festivals/blob/master/grails-app/services/ie/festivals/ArtistVideoService.groovy) 
and all references to it.

#### lastFM API Key

To retrieve artist data and images from the [last.fm API](http://www.last.fm/api) you will need to add the following:

````
festival.lastFM.apiKey=changeme
````

#### Booking.com

To earn commission when users are successfully referred to booking.com, add the following:
 
````
festivals.bookingDotComAffiliateId=changeme
````
 
#### Mail Server Password
 
Configure the password for the account that the application uses to send email with
 
````
grails.mail.password=changeme
````

To disable email sending, set `festival.sendEmail = false` in [Config.groovy](https://github.com/domurtag/festivals/blob/master/grails-app/conf/Config.groovy)

#### Janrain

[Janrain](http://janrain.com/product/social-login/) is used to allow users to register and login to the application 
using services such as Facebook, Twitter, Google+, etc. It requires
the following configuration

````
janrain.apiKey=changeme
janrain.applicationID=changeme
````

#### Airbrake API Key

Errors that occur within the application are recorded by [Airbake](https://airbrake.io/). To use this service, you must
add the following configuration

````
grails.plugins.airbrake.apiKey=8a01082544f249c44601e955e96efb8e
````

To disable Airbrake, simply remove the Grails Airbrake plugin.

#### Password Salt

Registered users' passwords are salted and hashed before being persistence. Set the password salt to a random string
via the setting below

````
systemWidePasswordSalt=choose-any-random-string-but-dont-ever-change-it
````
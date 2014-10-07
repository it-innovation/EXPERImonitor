The high-level ProvAPI
======================

Introduction
------------

In "Creating Provenance", the basic relations between the PROV elements have been explained. PROV is extremely flexible and can be used in a variety of different ways to represent the same thing. This creates the problem that - without standardisation - provenance models will look very different which in turn makes it difficult to analyse without knowing the exact data model. For this reason, we came up with the Experimedia provenance model. It basiclly uses the standard PROV elements by providing a wrapper to fit the general experiment theme. This has the huge advantage of a homogeneous database after the experiment to facilitate better, faster and more reliable queries.

**TODO**: insert high-level prov pictures here

There are 5 basic Experimedia classes and 1 original PROV class used in this data model:

*	**Service**: A service which is provides content for applications. There is typically some QoS associated with a service.
*	**Application**: An application used by a participant, often to retrieve content from a service.
*	**Participant**: A person participating in an experiment.
*	**Content**: A piece of information, which can be used by the articipant or passed between application and service.
*	**Entity**: A real world entity (like e.g. a skilift).
*	**prov:Activity**: An activity which is done by a participant, application or service. It might make use of a service, application or content.

	
**Example:**
	
**TODO**: picture here of one of the scenarios (e.g. Bob sending a tweet to the twitter service?)

The code
--------

This section shows an example of creating provenance statements using the ExperimediaFactory. Internally, it keeps an EDMProvFactory, which is created when conrtsucting an ExperimediaFactory instance: ::

	ExperimediaFactory factory = new ExperimediaFactory("prefix", "http://my.uri/ns#");
	
There are methods to create all the necessary high-level objects: ::

	Participant bob = factory.createParticipant("Bob");
	
	Service twitterService = factory.createService("TwitterService", "description here");
	
	Entity lift = factory.createEntity("Skilift123", "Skilift Alpenrausch in Schladming");
	
Note that when creating applications, the activity has an optional argument duration, which is in seconds and describes the activity of creating the entity. The timestamp is a unix timestamp without the milliseconds.::

	Application twitterApp = factory.createApplication(bob, "TwitterApp", "1410941680", "10");
	
Then there is a variety of methods to create the Experimedia Provenance patterns. Like the createApplication(...) method, all of them have an optional duration argument. If left out, a discrete activity will be created, meaning that start and end date are identical. ::
	
	Content photo = factory.createDataOnClient(bob, cameraApp, "newPhoto_72634876324", "1410941780");

	Activity lookAtPhoto = factory.useDataOnClient(bob, cameraApp, photo, "Bobs photo", "1410941789");
	
	Activity uselift = factory.useRealWorldEntity(bob, lift, "1410941889");

	Activity lookAtPhotos = factory.navigateClient(bob, cameraApp, "1410941915");

	Content hotTweets = factory.retrieveDataFromService(bob, twitterApp, twitterService, "Retrieve HotTweets", "1410942001");

	Content tweet = factory.createDataAtService(bob, twitterApp, twitterService, "Send a tweet", "1410942091");
	
Finally, after filling the ExperimediaFactory with data in most cases you want to get hold of the EDMProvReport. This is done by calling the getFactory() method to get hold of the underlying EDMProvFactory which provides all the necessary tools: ::

	EDMProvFactory provFactory = factory.getProvFactory();


Special cases and advanced programming
--------------------------------------

While it is highly encouraged to only use the ExperimediaFactory to create provenance, we acknowledge there might be the need for special modelling which is not covered. In this case there is good news: Under the hood, the ExperimediaFactory creates nothing else than EXPERImonitor Provenance API objects, wrapped in convenien methods to easily create the required patterns. The basic elements are still accessible and can be used as before to attach additional information.

If we created for example a participant like this ::

	Participant bob = factory.createParticipant("Robert Smith");

we might want to add FOAF information. In this case, we can easily get hold of the underlying lower level proveance API like this ::

	bob.agent.addTriple("foaf:knows", "http://my.uri/ns#Alice", EDMTriple.TRIPLE_TYPE.OBJECT_PROPERTY);
	
The - in this case - contained agent allows access to all the other methods of EDMAgent. The Experimedia types contain the following internal provenance API classes:

*	**Service**: EDMEntity
*	**Application**: EDMEntity (to be used by activities), EDMAgent (to perform Activities). Note that these two Provenance types are connected via owl:sameAs
*	**Participant**: EDMAgent
*	**Content**: EDMEntity
*	**Entity**: EDMEntity
*	**prov:Activity**: EDMActivity


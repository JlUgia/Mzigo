Nzigo

	- Listeners register and deregister themselves (that can be activities, classes, singletons etc)
	- They send Request(s) specifying what they expect back and that is send to them through a general callback specified after registration (interface on top)

	- Lib is a pool of threads for rest with priority. A request tracker based on url + querystring and method that returns to all interested listeners. Interested listeners are decided upon url path + method.

	- Set timeout expiry and return to client.
	- Set up http client on demand? or with builder (client must set up on Application subclass)
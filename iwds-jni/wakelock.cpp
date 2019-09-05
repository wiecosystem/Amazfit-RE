
jmethodID wake_lock_id;
const char sig[5] = {'(', 'I', ')', 'Z', 0x00};

// Apparently calls the java method
int iwds_acquire_wake_lock(int param)
{
	_jclass *var1;
	int var2;
	var1 = (_jclass*)getJenv((Iwds*)param);
	// int CallStaticBooleanMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...); (... = arguments)
	var2 = CallStaticBooleanMethod(var1, global_jclass, wake_lock_id, param);
	return !(var2 == 0)
}

class Server {
	void acquireWakeLockWithTimeout(Server *this, int param)
	{
		iwds_acquire_wake_lock(param);
		return;
	}
}

something classInitNative(_JNIEnv env, _jclass *jclass)
{
	string str[4];
	// Apparently GetMethodID from the JNIEnv class
	// jmethodID GetMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig);
	//DAT_0009911d0 = ( **(code **)(*(int*)env + 0x1c4))(env, jclass,"acquireWakeLock", &DAT_0008213d);
	wake_lock_id = env.GetMethodID(env, jclass, "acquireWakeLock", &sig)
	function1(str, "classInitNative GetMethodID acquireWakeLock succeeds", &jclass);
	// The &DAT_000911d4 is some global variable defined from the beginning by JNI_OnLoad
	//Iwds::Log::e(std::basic_string<char, std::char_traits<char>, std::allocator<char>> const&, std::basic_string<char, std::char_traits<char>, std::allocator<char>> const&)
	e((string*)&DAT_000911d4, str);
	function2(str);

	// Apparently the two sub functions are usless, it's mostly debug
}

// concat?!?
something *function1(something *param1, char *param2, something *param3)
{
	size_t size;
	something var2;
	char *var3;

	if (param2 == 0x00)
		var3 = 0xffffffff;
	else
	{
		size = strlen(param2);
		var3 = param2 + size;
	}
	var2 = function1_1(param2, var3, param3, 0);
	*param1 = var2;
	return param1;
}

// This is called at the very end of client.run(Client *this)
class ConnectWatchDog
{
	ConnectWatchDog *~ConnectWatchDog(ConnectWatchDog *this)
	{
		requestExitAndWait((Thread*)this);
		del Thread((Thread*)this);
		return this;
	}
}

//WaitConnectWatchDog is called at the end of server.run(Server *this)

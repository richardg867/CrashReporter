import os, shutil, socket, subprocess, sys, time

MOD_NAME = 'CrashReporter'
MOD_VERSION = '1.0'
MOD_FILE = os.path.join('crashreporter', 'core', 'CrashReporter.java')
MOD_NOTICE = 'An open source creation by Ferris Wheel Modding: ' \
	'https://github.com/richardg867/CrashReporter'

# FORGE_PATH should have forge.py, forgeversion.properties, install.cmd, etc.
FORGE_PATH = 'D:\\Minecraft\\MCP\\CrashReporter'

MONTHS = [None, 'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep',
	'Oct', 'Nov', 'Dec']

# Following two functions based on FML's implementation of:
# http://stackoverflow.com/questions/7545299/distutil-shutil-copytree
def _mkdir(newdir):
	if os.path.isdir(newdir):
		pass
	elif os.path.isfile(newdir):
		raise OSError('File with same name as directory found')
	else:
		head, tail = os.path.split(newdir)
		if head and not os.path.isdir(head):
			_mkdir(head)
		if tail:
			os.mkdir(newdir)

def copytree(src, dst, symlinks=False):	
	names = os.listdir(src)
	_mkdir(dst)
	errors = []
	for name in names:
		srcname = os.path.join(src, name)
		dstname = os.path.join(dst, name)
		try:
			if symlinks and os.path.islink(srcname):
				linkto = os.readlink(srcname)
				os.symlink(linkto, dstname)
			elif os.path.isdir(srcname):
				copytree(srcname, dstname, symlinks)
			else:
				shutil.copy2(srcname, dstname)
		except (IOError, os.error), why:
			errors.append((srcname, dstname, str(why)))
		except Exception, err:
			errors.extend(err.args[0])

def dummy_exit(code=0):
	pass

def main():
	build_time = time.gmtime()
	
	rev_head = open(os.path.join('.git', 'head'), 'r')
	if rev_head:
		rev_ref = open(
			os.path.join('.git', rev_head.read()[5:].rstrip('\r\n')),
			'r')
		if rev_ref:
			revision = 'revision ' + rev_ref.read()[:10]
	else:
		revision = 'local source tree'
	
	mcp_versioncfg = open(
		os.path.join(FORGE_PATH, 'mcp', 'conf', 'version.cfg'),
		'r')
	if mcp_versioncfg:
		for line in mcp_versioncfg:
			if line[:10] == 'MCPVersion':
				mcp_version = line.split('=')[1].strip(' \r\n')
			elif line[:13] == 'ClientVersion':
				mcp_mcversion = line.split('=')[1].strip(' \r\n')
		mcp_versioncfg.close()
	if mcp_version != None and mcp_mcversion != None:
		mcp_ver = 'MCP {0} for Minecraft {1}'.format(
			mcp_version, mcp_mcversion)
	else:
		mcp_ver = 'Unknown MCP version'
	
	forge_version = [None, None, None, None]
	forge_versionprops = open(
		os.path.join(FORGE_PATH, 'forgeversion.properties'))
	if forge_versionprops:
		for line in forge_versionprops:
			if line[:18] == 'forge.major.number':
				forge_version[0] = line.split('=')[1].strip(' \r\n')
			elif line[:18] == 'forge.minor.number':
				forge_version[1] = line.split('=')[1].strip(' \r\n')
			elif line[:21] == 'forge.revision.number':
				forge_version[2] = line.split('=')[1].strip(' \r\n')
			elif line[:18] == 'forge.build.number':
				forge_version[3] = line.split('=')[1].strip(' \r\n')
		forge_versionprops.close()
	if None not in forge_version:
		forge_ver = 'Forge {0[0]}.{0[1]}.{0[2]}.{0[3]}'.format(forge_version)
	else:
		forge_ver = 'Unknown Forge version'
	
	try:
		jdk_ver = subprocess.check_output('javac -version',
			stderr=subprocess.STDOUT)
	except CalledProcessError:
		jdk_ver = 'Unknown'
	
	info = open('buildinfo.txt', 'w')	
	info.write('{0} v{1} ({2})\n'.format(MOD_NAME, MOD_VERSION, revision))
	info.write(MOD_NOTICE + '\n')
	info.write('\n')
	info.write(
		'Built {0} {1} {2} {3:02d}:{4:=02d}:{5:02d} by {6}@{7}\n'.format(
		MONTHS[build_time.tm_mon], build_time.tm_mday, build_time.tm_year,
		build_time.tm_hour, build_time.tm_min, build_time.tm_sec,
		os.getenv('USERNAME'), socket.gethostname()))
	info.write('\n')
	info.write(mcp_ver + '\n')
	info.write(forge_ver + '\n')
	info.write('JDK: {0}\n'.format(jdk_ver))
	info.close()
	
	# actual building begins here
	print '*** Backing up Minecraft source...'
	copytree(os.path.join(FORGE_PATH, 'mcp', 'src', 'minecraft'), 
		os.path.join(FORGE_PATH, 'mcp', 'src', 'minecraft_backup'))
	
	print '*** Copying source...'
	copytree('src', os.path.join(FORGE_PATH, 'mcp', 'src', 'minecraft'))
	
	print '*** Injecting version...'
	mod_path = os.path.join(FORGE_PATH, 'mcp', 'src', 'minecraft', MOD_FILE)
	shutil.move(mod_path, mod_path + '.old')
	new_mod = open(mod_path, 'w')
	for line in open(mod_path + '.old', 'r'):
		new_mod.write(line.replace('@VERSION@', MOD_VERSION))
	new_mod.close()
	
	print '*** Calling MCP...'
	print ''
	
	# All of this is a big dirty hack... I'm *trying* to find a better way with
	# Searge/ProfMobius, but so far nothing happened
	cwd = os.getcwd()
	os.chdir(os.path.join(FORGE_PATH, 'mcp'))
	
	prev_exit = sys.exit
	sys.exit = dummy_exit
	
	try:
		sys.path.append(os.getcwd())
		import runtime.recompile, runtime.reobfuscate
	except ImportError:
		print '*** Failed to import MCP'
		raw_input()
		return
	
	runtime.recompile.main()
	sys.argv.append('--srgnames') # and this is the worst
	runtime.reobfuscate.main()    # part of this hack
	
	os.chdir(cwd)
	sys.exit = prev_exit
	
	print ''
	print '*** Restoring source...'
	shutil.rmtree(os.path.join(FORGE_PATH, 'mcp', 'src', 'minecraft'))
	shutil.move(os.path.join(FORGE_PATH, 'mcp', 'src', 'minecraft_backup'), 
		os.path.join(FORGE_PATH, 'mcp', 'src', 'minecraft'))
	
	print '*** Creating jar...'
	zip_name = '{0}-{1}.zip'.format(MOD_NAME.lower(), MOD_VERSION)
	subprocess.check_output(['7z', 'a', zip_name,
		os.path.join(FORGE_PATH, 'mcp', 'reobf', 'minecraft', '*')])
	subprocess.check_output(['7z', 'a', zip_name,
		os.path.join(os.getcwd(), 'resources', '*')])
	subprocess.check_output(['7z', 'a', zip_name,
		'buildinfo.txt'])
	shutil.move(zip_name, zip_name[:-4] + '.jar')
	
	print '*** Build complete'

if __name__ == '__main__':
	main()
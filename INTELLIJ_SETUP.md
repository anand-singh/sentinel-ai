# IntelliJ IDEA - Maven Module Setup Instructions

## ⚠️ IDE Reindex Required

After the module migration, IntelliJ IDEA needs to reimport the Maven projects to recognize the new module structure.

## Steps to Fix IDE Errors

### Option 1: Reload All Maven Projects (Recommended)
1. Open the **Maven** tool window (View → Tool Windows → Maven)
2. Click the **Reload All Maven Projects** button (circular arrows icon)
3. Wait for IntelliJ to reindex the project

### Option 2: Reimport from Root POM
1. Right-click on `/Users/hm02pk/Developer/ING/projects/google-ai/sentinel-ai/pom.xml`
2. Select **Maven** → **Reload Project**
3. IntelliJ will detect both modules

### Option 3: File Menu
1. Go to **File** → **Invalidate Caches...**
2. Check **Invalidate and Restart**
3. After restart, open the root `pom.xml`
4. Right-click → **Add as Maven Project**

### Option 4: Command Line Approach
```bash
cd /Users/hm02pk/Developer/ING/projects/google-ai/sentinel-ai
mvn idea:idea
```
Then restart IntelliJ IDEA.

## Verify Module Detection

After reloading, you should see in the **Project** tool window:
```
sentinel-ai
├── sentinel-ai-agent     [sentinel-ai-agent]
│   └── src
│       ├── main.java
│       └── test.java
└── api                   [sentinel-ai-api]
    └── src
        ├── main.java
        └── test.java
```

In the **Maven** tool window, you should see:
```
sentinel-ai-parent
├── sentinel-ai-agent
│   └── Lifecycle, Plugins
└── sentinel-ai-api
    └── Lifecycle, Plugins
```

## Common Issues

### Issue: "Cannot resolve symbol 'agent'"
**Cause**: IntelliJ hasn't recognized the sentinel-ai-agent module as a dependency

**Fix**: 
1. Reload Maven projects (Option 1 above)
2. If still failing, run `mvn clean install` from root
3. Then reload Maven projects again

### Issue: Red imports in OrchestratorService.java
**Cause**: IDE cache needs refresh

**Fix**:
1. File → Invalidate Caches → Invalidate and Restart
2. After restart, reload Maven projects

### Issue: Module "sentinel-ai-agent" not recognized
**Cause**: IntelliJ is still looking at the old single-module structure

**Fix**:
1. Close the project
2. Open the **root** `pom.xml` as a project (not the api folder)
3. IntelliJ will auto-detect both modules

## Verify Build Works

Even if IDE shows errors, verify that Maven build succeeds:

```bash
cd /Users/hm02pk/Developer/ING/projects/google-ai/sentinel-ai
mvn clean install
```

If Maven build succeeds but IDE shows errors, it's purely an IDE indexing issue.

## Expected IDE Behavior After Fix

✅ No red imports in any Java files
✅ Auto-completion works for `com.ing.sentinel.agent.*` packages
✅ Can navigate from API to agent module classes (Cmd+Click)
✅ Both modules appear in Maven tool window
✅ Tests can be run from IDE (green play buttons)

## Alternative: Open Correct Root

If IntelliJ is still showing errors:
1. Close current project
2. File → Open
3. Navigate to `/Users/hm02pk/Developer/ING/projects/google-ai/sentinel-ai/`
4. Select `pom.xml` (the parent POM)
5. Open as Project
6. IntelliJ will automatically recognize both modules

---

**Note**: The Maven build is successful (all 521 tests passing). IDE errors are cosmetic and will disappear after proper Maven reload.


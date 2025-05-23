#include <android/native_activity.h>
#include <android/log.h>
#include <EGL/egl.h>
#include <dlfcn.h>
#include <dobby.h>
#include <string>
#include <jni.h>
#include <GLES3/gl3.h>
#include <imgui/imgui.h>
#include <imgui/backends/imgui_impl_android.h>
#include <imgui/backends/imgui_impl_opengl3.h>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "TacoClient", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "TacoClient", __VA_ARGS__)

#define MINECRAFTPE_L "libminecraftpe.so"
#define EGL_L "libEGL.so"

typedef EGLBoolean(*eglSwapBuffers_t)(EGLDisplay display, EGLSurface surface);
typedef void(*ANativeActivity_onCreate_t)(ANativeActivity *, void *, size_t);
typedef void(*android_main_t)(struct android_app *);

static eglSwapBuffers_t eglSwapBuffers_original = nullptr;
static ANativeActivity_onCreate_t ANativeActivity_onCreate_original = nullptr;
static android_main_t android_main_original = nullptr;
static bool setup_initialized = false;

void setup_imgui_renderer(EGLDisplay display, EGLSurface surface) {
    if (setup_initialized) {
        return;
    }

    if (display == nullptr || surface == nullptr) {
        return;
    }

    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO& io = ImGui::GetIO();

    // Initialize backends
    ImGui_ImplAndroid_Init(nullptr); // Will need proper ANativeWindow* later
    ImGui_ImplOpenGL3_Init("#version 300 es"); // Use ES for Android

    setup_initialized = true;
}

void imgui_render(EGLDisplay display, EGLSurface surface) {
    if (!setup_initialized) {
        return;
    }

    if (display == nullptr || surface == nullptr) {
        return;
    }

    ImGuiIO& io = ImGui::GetIO();

    // Setup display size (you might want to get this from the surface properly)
    io.DisplaySize = ImVec2(1280, 720); // Temporary values - need proper implementation

    // Start new frame
    ImGui_ImplOpenGL3_NewFrame();
    ImGui_ImplAndroid_NewFrame();
    ImGui::NewFrame();

    // Your ImGui content
    ImGui::ShowDemoWindow();

    // Rendering
    ImGui::Render();
    glViewport(0, 0, (int)io.DisplaySize.x, (int)io.DisplaySize.y);
    ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
}

EGLBoolean eglSwapBuffers_hook(EGLDisplay display, EGLSurface surface) {
    LOGI("display: %p; surface: %p", display, surface);
    setup_imgui_renderer(display, surface);
    imgui_render(display, surface);
    return eglSwapBuffers_original(display, surface);
}

extern "C" void ANativeActivity_onCreate(ANativeActivity *activity, void *saved_state, size_t saved_state_size) {
    if (ANativeActivity_onCreate_original) {
        ANativeActivity_onCreate_original(activity, saved_state, saved_state_size);
    }
}

extern "C" void android_main(struct android_app *app) {
    if (android_main_original) {
        android_main_original(app);
    }
}

jint JNI_DoHooks() {
    int hook = DobbyHook(DobbySymbolResolver(EGL_L, "eglSwapBuffers"), 
                        reinterpret_cast<void *>(eglSwapBuffers_hook), 
                        reinterpret_cast<void **>(&eglSwapBuffers_original));

    if (hook != 0) {
        LOGE("Failed to hook eglSwapBuffers");
        return JNI_ERR;
    }

    return JNI_OK;
}

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    void *minecraftpe = dlopen(MINECRAFTPE_L, RTLD_LAZY);
    void *egl = dlopen(EGL_L, RTLD_LAZY);

    if (minecraftpe) {
        void *ANativeActivity_onCreate_sym = DobbySymbolResolver(MINECRAFTPE_L, "ANativeActivity_onCreate");
        void *android_main_sym = DobbySymbolResolver(MINECRAFTPE_L, "android_main");

        if (!ANativeActivity_onCreate_sym) {
            LOGE("Failed to find ANativeActivity_onCreate");
            return JNI_ERR;
        }

        if (!android_main_sym) {
            LOGE("Failed to find android_main");
            return JNI_ERR;
        }

        ANativeActivity_onCreate_original = reinterpret_cast<ANativeActivity_onCreate_t>(ANativeActivity_onCreate_sym);
        android_main_original = reinterpret_cast<android_main_t>(android_main_sym);
    } else {
        LOGE("Failed to find libminecraftpe.so");
        return JNI_ERR;
    }

    if (egl) {
        void *eglSwapBuffers_sym = DobbySymbolResolver(EGL_L, "eglSwapBuffers");

        if (!eglSwapBuffers_sym) {
            LOGE("Failed to find eglSwapBuffers");
            return JNI_ERR;
        }

        eglSwapBuffers_original = reinterpret_cast<eglSwapBuffers_t>(eglSwapBuffers_sym);
    } else {
        LOGE("Failed to find libEGL.so");
        return JNI_ERR;
    }

    JNI_DoHooks();

    return JNI_VERSION_1_6;
}
package com.example.lib;

import com.example.annotation.MyOverride;
import com.google.auto.service.AutoService;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class MyOverrideProcessor extends AbstractProcessor {
    private Messager messager;
    private Elements elementUtils;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(MyOverride.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elesWithBind = roundEnv.getElementsAnnotatedWith(MyOverride.class);

        for (Element element : elesWithBind) {
            checkAnnotationValid(element, MyOverride.class);
            ExecutableElement executableElement = (ExecutableElement) element;
            TypeElement classElement = (TypeElement) executableElement.getEnclosingElement();
            if (!isOverriding(executableElement, classElement)) {
                error(executableElement, "该方法不是重写方法，不能使用MyOverride注解！");
            }
        }
        return true;
    }

    public boolean isOverriding(ExecutableElement method, TypeElement base) {
        List<? extends TypeMirror> list = base.getInterfaces();
        if (list != null && list.size() > 0) {
            for (TypeMirror type : list) {
                if (isOverridesInTypeMirror(method, base, type)) return true;
            }
        }

        TypeMirror superClass = base.getSuperclass();
        return isOverridesInTypeMirror(method, base, superClass);
    }

    private boolean isOverridesInTypeMirror(ExecutableElement method, TypeElement base,
        TypeMirror type) {
        List<ExecutableElement> executableElements =
            ElementFilter.methodsIn(typeUtils.asElement(type).getEnclosedElements());
        if (executableElements != null && executableElements.size() > 0) {
            for (ExecutableElement executableElement : executableElements) {
                if (elementUtils.overrides(method, executableElement, base)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkAnnotationValid(Element annotatedElement, Class clazz) {
        if (annotatedElement.getKind() != ElementKind.METHOD) {
            error(annotatedElement, "%s 的方法才能使用", clazz.getSimpleName());
            return false;
        }
        return true;
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message, element);
    }
}

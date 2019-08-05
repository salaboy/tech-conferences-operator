package com.salaboy.conferences.crds.tekton.task;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.salaboy.conferences.crds.tekton.EnvVar;

import java.util.Arrays;
import java.util.List;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskStep {
    private List<String> args;
    private List<String> command;
    private List<EnvVar> env;
    private String name;
    private String image;
    private String workingDir;

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public List<String> getCommand() {
        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public List<EnvVar> getEnv() {
        return env;
    }

    public void setEnv(List<EnvVar> env) {
        this.env = env;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    @Override
    public String toString() {
        return "Step{" +
                "args=" + ((args != null) ? Arrays.toString(args.toArray()) : args) +
                ", command=" + ((command != null) ? Arrays.toString(command.toArray()) : command) +
                ", env=" + ((env != null) ? Arrays.toString(env.toArray()) : env) +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", workingDir='" + workingDir + '\'' +
                '}';
    }
}

package com.example.app1xmlreceiver.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "Data")
public class DataMessage {
    
    @JacksonXmlProperty(localName = "Method")
    private Method method;
    
    @JacksonXmlProperty(localName = "Process")
    private Process process;
    
    @JacksonXmlProperty(localName = "Layer")
    private String layer;
    
    @JacksonXmlProperty(localName = "Creation")
    private Creation creation;
    
    @JacksonXmlProperty(localName = "Type")
    private String type;
    
    // Constructors
    public DataMessage() {}
    
    // Getters and Setters
    public Method getMethod() {
        return method;
    }
    
    public void setMethod(Method method) {
        this.method = method;
    }
    
    public Process getProcess() {
        return process;
    }
    
    public void setProcess(Process process) {
        this.process = process;
    }
    
    public String getLayer() {
        return layer;
    }
    
    public void setLayer(String layer) {
        this.layer = layer;
    }
    
    public Creation getCreation() {
        return creation;
    }
    
    public void setCreation(Creation creation) {
        this.creation = creation;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    // Inner classes
    public static class Method {
        @JacksonXmlProperty(localName = "Name")
        private String name;
        
        @JacksonXmlProperty(localName = "Type")
        private String type;
        
        @JacksonXmlProperty(localName = "Assembly")
        private String assembly;
        
        // Constructors
        public Method() {}
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getAssembly() {
            return assembly;
        }
        
        public void setAssembly(String assembly) {
            this.assembly = assembly;
        }
    }
    
    public static class Process {
        @JacksonXmlProperty(localName = "Name")
        private String name;
        
        @JacksonXmlProperty(localName = "Id")
        private String id;
        
        @JacksonXmlProperty(localName = "Start")
        private Start start;
        
        // Constructors
        public Process() {}
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public Start getStart() {
            return start;
        }
        
        public void setStart(Start start) {
            this.start = start;
        }
    }
    
    public static class Start {
        @JacksonXmlProperty(localName = "Epoch")
        private String epoch;
        
        @JacksonXmlProperty(localName = "Date")
        private String date;
        
        // Constructors
        public Start() {}
        
        // Getters and Setters
        public String getEpoch() {
            return epoch;
        }
        
        public void setEpoch(String epoch) {
            this.epoch = epoch;
        }
        
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
    }
    
    public static class Creation {
        @JacksonXmlProperty(localName = "Epoch")
        private String epoch;
        
        @JacksonXmlProperty(localName = "Date")
        private String date;
        
        // Constructors
        public Creation() {}
        
        // Getters and Setters
        public String getEpoch() {
            return epoch;
        }
        
        public void setEpoch(String epoch) {
            this.epoch = epoch;
        }
        
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
    }
}

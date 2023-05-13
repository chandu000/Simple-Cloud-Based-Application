package client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.System.arraycopy;

public class StartSync {

    private Thread thread1;
    private Thread thread2;
    private String clientJsonPath;
    private String serverJsonPath;
    private String clientPath;
    private String serverPath;

    public int trans_Size;

    public StartSync() {
        trans_Size = 50000;
        thread1 = new Thread(upload);
        thread2 = new Thread(download);
        clientJsonPath = "/Users/chandupavanbudda/Desktop/file  sync/src/client 2/.metafile.json";
        serverJsonPath = "/Users/chandupavanbudda/Desktop/file  sync/src/server folder/.metafile.json";
        clientPath = "/Users/chandupavanbudda/Desktop/file  sync/src/client 2/";
        serverPath = "/Users/chandupavanbudda/Desktop/file  sync/src/server folder/";
        File sfile = new File(serverJsonPath);
        File cfile = new File(clientJsonPath);
        try {
            sfile.createNewFile();
            cfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void starttask1() {
        thread1.start();
    }

    public void starttask2() {
        thread2.start();
    }
    Runnable upload = this::run1;
    Runnable download = this::run2;
    public void sendData(File file) {
        long source_file_size = file.length();
        int byte_per_split = trans_Size;
        long no_of_split = source_file_size/byte_per_split;
        int rem_bytes = (int) source_file_size % byte_per_split;
        InetAddress ia = null;
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();
            ia = InetAddress.getLocalHost();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        String sb = readDataFromFile(file);

        byte[] sentdata = sb.getBytes();
        String[] filename = file.getName().split("\\.");
        int part = 0;
        DatagramPacket[] p1 = new DatagramPacket[(int) no_of_split+1];
        DatagramPacket[] p2 = new DatagramPacket[(int) no_of_split+1];
        String subfile[] = new String[(int) no_of_split+1];
        String code[] = new String[(int) no_of_split+1];
        while(part < no_of_split) {
            subfile[part] = filename[0]+part+"."+filename[1];
            byte[] sentname = subfile[part].getBytes();
            p1[part] = new DatagramPacket(sentname,sentname.length,ia,1001);
            byte[] b = new byte[byte_per_split];
            arraycopy(sentdata, (part*byte_per_split), b, 0, byte_per_split);
            p2[part] = new DatagramPacket(b,b.length,ia,1001);
            String s = new String(b).trim();
            code[part] = bytesToHex(digest(s.getBytes(),"SHA-256"));
            part++;
        }
        if(rem_bytes > 0) {
            subfile[part] = filename[0]+part+"."+filename[1];
            byte[] sentname = subfile[part].getBytes();
            p1[part] = new DatagramPacket(sentname,sentname.length,ia,1001);
            byte[] b = new byte[rem_bytes];
            arraycopy(sentdata, (part*byte_per_split), b, 0, rem_bytes);
            p2[part] = new DatagramPacket(b,b.length,ia,1001);
            String s = new String(b).trim();
            code[part] = bytesToHex(digest(s.getBytes(),"SHA-256"));
            part++;
        }
        for(int i=0;i<part;i++) {
            try {
                ds.send(p1[i]);
                ds.send(p2[i]);
            }catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        ds.close();
        System.out.println(" \n data sent");
        recordData(file.getName(),subfile,code);
    }

    public void sendPartData(File file,HashMap<String,String> childmap) {
        long source_file_size = file.length();
        int byte_per_split = trans_Size;
        long no_of_split = source_file_size/byte_per_split;
        int rem_bytes = (int) source_file_size % byte_per_split;
        InetAddress ia = null;
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();
            ia = InetAddress.getLocalHost();
        } catch (UnknownHostException | SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String sb = readDataFromFile(file);

        byte[] sentdata = sb.getBytes();
        String[] filename = file.getName().split("\\.");
        int part = 0;
        DatagramPacket[] p1 = new DatagramPacket[(int) no_of_split+1];
        DatagramPacket[] p2 = new DatagramPacket[(int) no_of_split+1];
        String subfile[] = new String[(int) no_of_split+1];
        String code[] = new String[(int) no_of_split+1];
        while(part < no_of_split) {
            subfile[part] = filename[0]+part+"."+filename[1];
            byte[] sentname = subfile[part].getBytes();
            p1[part] = new DatagramPacket(sentname,sentname.length,ia,1001);
            byte[] b = new byte[byte_per_split];
            arraycopy(sentdata, (part*byte_per_split), b, 0, byte_per_split);
            p2[part] = new DatagramPacket(b,b.length,ia,1001);
            String s = new String(b).trim();
            code[part] = bytesToHex(digest(s.getBytes(),"SHA-256"));
            part++;
        }
        if(rem_bytes > 0) {
            subfile[part] = filename[0]+part+"."+filename[1];
            byte[] sentname = subfile[part].getBytes();
            p1[part] = new DatagramPacket(sentname,sentname.length,ia,1001);
            byte[] b = new byte[rem_bytes];
            arraycopy(sentdata, (part*byte_per_split), b, 0, rem_bytes);
            p2[part] = new DatagramPacket(b,b.length,ia,1001);
            String s = new String(b).trim();
            code[part] = bytesToHex(digest(s.getBytes(),"SHA-256"));
            part++;
        }
        for(int i=0;i<part;i++) {
            if(childmap.get(subfile[i]) == null || (childmap.get(subfile[i]) != null && !(childmap.get(subfile[i]).equals(code[i])))) {
                try {
                    ds.send(p1[i]);
                    ds.send(p2[i]);

                    System.out.println(" \n send:"+subfile[i]+","+code[i]);
                }catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
        ds.close();

        System.out.println("part data sent");
        recordData(file.getName(),subfile,code);
    }

    public byte[] digest(byte[] input, String algorithm) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        byte[] result = md.digest(input);
        return result;
    }
    public static String bytesToHex(byte[] bytes) {
        return new BigInteger(1, bytes).toString(16);
    }



    public void recordData(String filename,String[] subfile,String[] code) {
        File tempfile = new File(clientJsonPath);
        HashMap<String,LinkedHashMap<String,String>> hmap = readJsonFile(tempfile);
        JSONArray jsonArray = new JSONArray();
        LinkedHashMap<String,String> child = new LinkedHashMap<>();
        for(int i=0;i<subfile.length;i++) {
            child.put(subfile[i],code[i]);
        }
        hmap.put(filename, child);
        jsonArray.add(hmap);
        System.out.println(jsonArray);
        writeDataToJson(clientJsonPath,jsonArray);
    }
    public void writeDataToJson(String path, JSONArray jsonArray) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(jsonArray.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, LinkedHashMap<String,String>> readJsonFile(File file){
        String tempdata = "";
        try {
            tempdata = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HashMap<String,LinkedHashMap<String,String>> hmap = new HashMap<>();
        if(tempdata.length() > 0) {
            JSONArray jsonArray = null;
            try {
                jsonArray = (JSONArray) new JSONParser().parse(tempdata);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            for(int i=0;i<jsonArray.size();i++) {
                JSONObject json = (JSONObject) jsonArray.get(i);
                HashSet<String> keys = new HashSet<>(json.keySet());
                for(String s : keys) {
                    JSONObject cjson = (JSONObject) json.get(s);
                    try {
                        LinkedHashMap<String,String> t = (LinkedHashMap<String, String>) new ObjectMapper().readValue(cjson.toString(), Map.class);
                        hmap.put(s,t);
                    } catch (JsonMappingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (JsonProcessingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        }
        return hmap;
    }
    public void readServerFile(String root,LinkedHashMap<String,String> child) {
        String clientfilepath = clientPath+root;
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<child.size();i++) {
            String[] subfile = root.split("\\.");
            String path = serverPath+subfile[0]+i+"."+subfile[1];
            File file = new File(path);
            String s = readDataFromFile(file).trim();
            sb.append(s);
        }
        PrintWriter pw = null;

        try {
            pw = new PrintWriter(clientfilepath);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pw.println(sb.toString());
        pw.close();
        updatejson(root,child);
    }
    public String readDataFromFile(File file) {
        FileReader fileReader = null;
        StringBuilder sb = new StringBuilder();
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(fileReader);
        String line;
        try {
            while((line=br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    public void updatejson(String root,LinkedHashMap<String,String> child) {
        File tempfile = new File(clientJsonPath);
        HashMap<String,LinkedHashMap<String,String>> clientMap = readJsonFile(tempfile);
        clientMap.put(root, child);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(clientMap);

        try (PrintWriter out = new PrintWriter(new FileWriter(clientJsonPath))) {
            out.write(jsonArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void run1(){
        File tempfile = new File(serverJsonPath);
        HashMap<String,LinkedHashMap<String,String>> hmap = readJsonFile(tempfile);

        File files = new File(clientPath);
        for(File file : files.listFiles()) {
            if(!file.getName().startsWith(".")) {
                HashMap<String,String> chmap = hmap.get(file.getName());
                if(chmap != null) {
                    sendPartData(file,chmap);
                }else {
                    sendData(file);
                }
            }
        }
    }

    private void run2() {
        File tempfile = new File(clientJsonPath);
        HashMap<String,LinkedHashMap<String,String>> clientMap = readJsonFile(tempfile);
        tempfile = new File(serverJsonPath);
        HashMap<String,LinkedHashMap<String,String>> serverMap = readJsonFile(tempfile);
        for(Map.Entry<String, LinkedHashMap<String,String>> t : serverMap.entrySet()) {
            if(clientMap.get(t.getKey()) == null) {
                /* file not exist in client download it*/
                readServerFile(t.getKey(),serverMap.get(t.getKey()));
            }else {
                HashMap<String,String> ser = serverMap.get(t.getKey());
                HashMap<String,String> cli = clientMap.get(t.getKey());
                for(Map.Entry<String, String> te : ser.entrySet()) {
                    if(cli.get(te.getKey()) == null || !(ser.get(te.getKey()).equals(cli.get(te.getKey())))){
                        /* file not exist in client download it */
                        readServerFile(t.getKey(),serverMap.get(t.getKey()));
                        break;
                    }
                }
            }
        }
    }
}

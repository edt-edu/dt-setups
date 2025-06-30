echo "Installation des Turtlebot3-Workspace für den SBC."
echo ""
echo "Benötigtes Betriebssystem >>> Ubuntu Server 22.04.2 (Jammy Jellyfish)"
echo "Workspace Verzeichnise >>> $home/turtlebot3_ws"
echo ""
echo "[ENTER] drücken um die Installation zu beginnen."
echo "[STRG] + [C] drücken um Abbzubrechen."
read

echo ""
echo "[Setze ROS-Version und Name des Workspace]"
name_ros_version=${name_ros_version:="humble"}
name_workspace=${name_workspace:="turtlebot3_ws"}

echo ""
echo "[Setze Lokalisierung]"
echo ""
locale
sudo apt update && sudo apt install locales
sudo locale-gen en_US en_US.UTF-8
sudo update-locale LC_ALL=en_US.UTF-8 LANG=en_US.UTF-8
export LANG=en_US.UTF-8
locale

echo ""
echo "[Setze Quellen]"
echo ""
sudo apt install software-properties-common && sudo add-apt-repository universe
sudo apt update && sudo apt install curl -y
sudo curl -sSL https://raw.githubusercontent.com/ros/rosdistro/master/ros.key -o /usr/share/keyrings/ros-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/ros-archive-keyring.gpg] http://packages.ros.org/ros2/ubuntu $(. /etc/os-release && echo $UBUNTU_CODENAME) main" | sudo tee /etc/apt/sources.list.d/ros2.list > /dev/null

echo ""
echo "[Installation der ROS2 packages]"
echo ""
sudo apt update && sudo apt upgrade && sudo apt install -y ros-$name_ros_version-ros-base

echo ""
echo "[Installation zusätzlicher packages für den Turtlebot3]"
echo ""
sudo apt install python3-argcomplete python3-colcon-common-extensions libboost-system-dev build-essential
sudo apt install ros-$name_ros_version-hls-lfcd-lds-driver ros-$name_ros_version-turtlebot3-msgs ros-$name_ros_version-dynamixel-sdk ros-$name_ros_version-controller-interface ros-$name_ros_version-diff-drive-controller ros-$name_ros_version-gripper-controllers ros-$name_ros_version-xacro ros-$name_ros_version-imu-sensor-broadcaster ros-$name_ros_version-joint-trajectory-controller ros-$name_ros_version-controller-manager-msgs ros-$name_ros_version-controller-manager ros-$name_ros_version-moveit-planners-ompl ros-$name_ros_version-pilz-industrial-motion-planner ros-$name_ros_version-moveit-planners ros-$name_ros_version-moveit-simple-controller-manager ros-$name_ros_version-joint-state-broadcaster
sudo apt install libudev-dev

echo ""
echo "[Setze aliase in bashrc]"
echo ""
sh -c "echo \"alias nb='nano ~/.bashrc'\" >> ~/.bashrc"
sh -c "echo \"alias sb='source ~/.bashrc'\" >> ~/.bashrc"
sh -c "echo \"alias gs='git status'\" >> ~/.bashrc"
sh -c "echo \"alias gp='git pull'\" >> ~/.bashrc"

sh -c "echo \"alias cw='cd ~/$name_workspace'\" >> ~/.bashrc"
sh -c "echo \"alias cs='cd ~/$name_workspace/src'\" >> ~/.bashrc"
sh -c "echo \"alias cb='cd ~/$name_workspace && colcon build --symlink-install && source ~/.bashrc'\" >> ~/.bashrc"

sh -c "echo \"source /opt/ros/$name_ros_version/setup.bash\" >> ~/.bashrc"
sh -c "echo \"source ~/$name_workspace/install/local_setup.bash\" >> ~/.bashrc"

# !WICHTIG! Für verschiedene Turtlebot3 muss die ROS_DOMAIN_ID verschieden gesetzt werden.
sh -c "echo 'export ROS_DOMAIN_ID=30 #TURTLEBOT3' >> ~/.bashrc"
sh -c "echo \"export LDS_MODEL=LDS-02\" >> ~/.bashrc"

echo ""
echo "[Stelle OpenCR ein]"
echo ""
sudo dpkg --add-architecture armhf
sudo apt update
sudo apt install libc6:armhf
export OPENCR_PORT=/dev/ttyACM0
export OPENCR_MODEL=turtlebot3_manipulation
wget https://github.com/ROBOTIS-GIT/OpenCR-Binaries/raw/master/turtlebot3/ROS2/latest/opencr_update.tar.bz2
tar -xvf opencr_update.tar.bz2
cd ./opencr_update
./update.sh $OPENCR_PORT $OPENCR_MODEL.opencr
cd ~

echo ""
echo "[Umgebungssetup]"
echo ""
source /opt/ros/$name_ros_version/setup.sh

echo ""
echo "[Erstellung des Workspace und testen von colcon build]"
echo ""
mkdir -p $HOME/$name_workspace/src && cd ~/$name_workspace/src
git clone -b humble-devel https://github.com/ROBOTIS-GIT/turtlebot3_manipulation.git
git clone -b ros2-devel https://github.com/ROBOTIS-GIT/ld08_driver.git
cd ~/turtlebot3_ws/
colcon build --parallel-workers 1

echo ""
echo "[Installation abgeschlossen]"
echo ""
echo "Der nächste Schritt ist der Transfer der src Inhalts aus dem Git in den src-Ordner des nun erstellten Workspace mit anschließendem ausführen von colcon build."

exec bash
